(ns brave-and-true.ch11.core-async
  (:require [clojure.core.async
             :as a
             :refer :all])
  (:gen-class))

;;Go Blocks -- go blocks create a new process. They run concurrently on seperate threads

;;use the chan function to create a channel
(def echo-chan (chan))

;;this block says, when I take a message from echo-chan, print it
;;<! is the 'take' function
;;It listens to the channel you give it as an argument, and the process it
;; belongs to waits until another process puts a message on the channel.
;; When <! retrieves a value, the value is returned and 'println' is executed.
(go (println (<! echo-chan)))

;;put the string "ketchup" on 'echo-chan' and it then returns true
;;when you put a message on a channel, the process blocks until another process
;;takes the message. Here, the REPL doesn't have to wait, because there is a process
;;listening to the channel. '(>!! (chan) "mustard")' the REPL will block indefinitely
;;If we create a new channel and put something on it, but there’s no process
;; listening to that channel. Processes don’t just wait to
;; receive messages; they also wait for the messages they put on a channel to be taken.
(>!! echo-chan "ketchup")

;;Buffered Channels -- put 'n' values on the channel without waiting
;;this chan will accept 2 values without waiting
(def echo-buffer (chan 2))

(>!! echo-buffer "ketchup")
(>!! echo-buffer "mustard")
;;our channel only has a buffer size of 2, so this blocks the REPL again
;(>!! echo-buffer "noooo")

;;2 other buffer types:
;;'sliding-buffer' -- drops values using FIFO (oldest is thrown away first)and
;;'dropping-buffer' -- drops values using LIFO (newest is dropped first)

;;there are multiple varities of put ( >! ) and take ( <! )
;;Inside go block	| Outside go block
;;put	  >! or >!!	| >!!
;;take	<! or <!! |	<!!

;;Waiting
;;2 varieties:
;;blocking -- thread stops execution until a task is complete. thread is alive,
;;  but not doing any work. you have to create a new thread if you want to
;;  keep working. (future)
;;parking -- free up the thread so it can keep working. ex. 2 processes A & B
;;  A is running on the thread and then waits for a put or take. clojure will
;;  move A off of the thread and B on to the thread. if B starts waiting and
;;  A's put or take has finished, clojure will move B off and put A back on
;;  the thread. This allows instructions from multiple processes to be
;;  interleave on a single thread.

;;Thread -- for those times when you want to use Blocking instead of Parking
;;  e.g. a process will take a long time before putting or taking.
;;  thread acts almost exactly like a future (create a new thread and execute
;;  a process on that thread), but instead of returning a an object, it returns
;;  a channel. When the new process stops, the return value is put on the channel
;;  that thread returns.
(thread (println (<!! echo-chan)))
(>!! echo-chan "mustard")

(let [t (thread "chili")]
  (<!! t))
;;  The reason to use thread over channel is so you don't clog your threadpool
;;  If you have so long running process (download a large file -> save file
;;  -> put file path on channel), while the processes are doing work, they
;;  can't be parked

;;put anything in and get a hotdog out
(defn hot-dog-machine
  []
  (let [in (chan)
        out (chan)]
    (go (<! in)
        (>! out "hot dog"))
    [in out]))

(let [[in out] (hot-dog-machine)]
  (>!! in "pocket lint")
  (<!! out))

;;specify number of hotdogs, only give them when someone enters 3
(defn hot-dog-machine-v2
  [hot-dog-count]
  (let [in (chan)
        out (chan)]
    (go (loop [hc hot-dog-count]
          (if (> hc 0)
            (let [input (<! in)]
              (if (= 3 input)
                 (do (>! out "hot dog")
                     (recur (dec hc)))
                 (do (>! out "wilted lettuce")
                     (recur hc))))
            (do (close! in)
                 (close! out)))))
    [in out]))

(let [[in out] (hot-dog-machine-v2 2)]
  (>!! in "pocket lint")
  (println (<!! out))

  (>!! in 3)
  (println (<!! out))

  (>!! in 3)
  (println (<!! out))

  (>!! in 3)
  (<!! out))

;;here we are using channels like pipe functions!
(let [c1 (chan)
      c2 (chan)
      c3 (chan)]
  (go (>! c2 (clojure.string/upper-case (<! c1))))
  (go (>! c3 (clojure.string/reverse (<! c2))))
  (go (println (<! c3)))
  (>!! c1 "redrum"))

;;alts!! -- takes a vector of channels as its args and says "try to do a blocking take
;;  on each one of these channels simultaneously. as soon as a take succeeds, return a
;;  vector whose first element is the value and second is the winning channel" It only
;;  takes a value from the first channel that is available. it does not touch the rest
(comment
  (defn upload
    [headshot c]
    (go (Thread/sleep (rand 100))
        (>! c headshot)))

  (let [c1 (chan)
        c2 (chan)
        c3 (chan)]
    (upload "serious.jpg" c1)
    (upload "fun.jpg" c2)
    (upload "sassy.jpg" c3)
    (let [[headshot channel] (alts!! [c1 c2 c3])]
      (println "Sending headshot notification for" headshot))))
; => Sending headshot notification for sassy.jpg

;;  One good use for this is to supply a timeout channel.
(comment
  (let [c1 (chan)]
    (upload "serious.jpg" c1)
    (let [[headshot channel] (alts!! [c1 (timeout 20)])]
      (if headshot
        (println "Sending headshot notification for" headshot)
        (println "Timed out!")))))
; => Timed out!

;;  you can also do a put by supplying a nested vector
(comment
  (let [c1 (chan)
        c2 (chan)]
    (go (<! c2))
       (let [[value channel] (alts!! [c1 [c2 "put!"]])]
          (println value)
          (= channel c2))))
;; => true
;; => true

;;queues --
(defn append-to-file
  "Write a string to the end of a file"
  [filename s]
  (spit filename s :append true))

(defn format-quote
  "Delineate the beginning and end of a quote because it's convenient"
  [quote]
  (str "=== BEGIN QUOTE ===\n" quote "=== END QUOTE ===\n\n"))

(defn random-quote
  "Retrieve a random quote and format it"
  []
  (format-quote (slurp "http://www.braveclojure.com/random-quote")))

(defn snag-quotes
  "1) create a channel that is shared between the quote producer and consumer
  2) create a process with an infinite loop
  3) on every iteration of the loop, wait for a quote to arrive on c and append it to the file
  4) create a num-quotes process that fetches a quote and then puts it on c
  * each task is handled in the order it is created ensuring only on write at a time"
  [filename num-quotes]
  (let [c (chan)]
    (go (while true (append-to-file filename (<! c))))
    (dotimes [n num-quotes] (go (>! c (random-quote))))))

;;Process pipelines
(defn upper-caser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/upper-case (<! in)))))
    out))

(defn reverser
  [in]
  (let [out (chan)]
    (go (while true (>! out (clojure.string/reverse (<! in)))))
    out))

(defn printer
  [in]
  (go (while true (println (<! in)))))

(def in-chan (chan))
(def upper-caser-out (upper-caser in-chan))
(def reverser-out (reverser upper-caser-out))
(printer reverser-out)

(>!! in-chan "redrum")
; => MURDER

(>!! in-chan "repaid")
; => DIAPER