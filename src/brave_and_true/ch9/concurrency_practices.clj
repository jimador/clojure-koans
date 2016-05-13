(ns brave-and-true.ch9.concurrency-practices)

;;Reference cell problem -- 2 threads reading and writing to the same location
;;Clojure has a few tools to deal with this: future, delay, promise

;;future
(future (Thread/sleep 4000)
        (println "I print after 4 seconds"))
(println "I print now!")

(let [result (future (println "this prints once")
                     (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result))

;;Dereferencing a future will block if the future hasn’t finished running
(let [result (future (Thread/sleep 3000)
                     (+ 1 1))]
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print"))

;;deref can take a timeout value
;;this says return the value 5 if the future doesn't return within
;;10ms
(deref (future (Thread/sleep 1000) 0) 10 5)

;;realize? will interrogate the future to see if it's done
(let [f (future)]
  @f
  (realized? f))
;;=> true

;;delay -- create task without having to execute it immediately
;;here, nothing is printed because we haven't said to eval the let
(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "First deref :" message)
           message)))

;;force -- starts the task
(force jackson-5-delay)

;;delay is evaluated only once and the result is cached
(println @jackson-5-delay)

;;a good use-case for this is to notify after the first in a group of promises
;;is resolved

;;deliver -- lets you deliver a result to a promise
(def my-promise (promise))
(deliver my-promise (+ 1 2))
@my-promise

(def yak-butter-international
  {:store      "Yak Butter International"
   :price      90
   :smoothness 90})
(def butter-than-nothing
  {:store      "Butter Than Nothing"
   :price      150
   :smoothness 83})
;; This is the butter that meets our requirements
(def baby-got-yak
  {:store      "Baby Got Yak"
   :price      94
   :smoothness 99})

(defn mock-api-call
  [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criteria, return the butter, else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

;;When you check each site synchronously,
;;it could take more than one second per site to obtain a result
(time (some (comp satisfactory? mock-api-call)
            [yak-butter-international butter-than-nothing baby-got-yak]))

;;use a promise and futures to perform each check on a separate thread.
(time
  (let [butter-promise (promise)]
    (doseq [butter [yak-butter-international butter-than-nothing baby-got-yak]]
      (future (if-let [satisfactory-butter (satisfactory? (mock-api-call butter))]
                (deliver butter-promise satisfactory-butter))))
    (println "And the winner is:" @butter-promise)))

;;you first create a promise, 'butter-promise', and then create three futures with access to that promise.
;;Each future’s task is to evaluate a yak butter site and to deliver the site’s data to the promise if
;;it’s satisfactory. Finally, you dereference butter-promise, causing the program to block until the site
;;data is delivered. This takes about one second instead of three because the site evaluations happen in
;;parallel. By decoupling the requirement for a result from how the result is actually computed,
;;you can perform multiple computations in parallel and save some time

;;PROBLEM if none of the butter is satisfactory, the deref will block forever.
;;gaurd against this with a timeout

(let [p (promise)]
  (deref p 100 "timed out"))

;;you can also use promises to register callbacks
(let [ferengi-wisdom-promise (promise)]
  (future (println "Here's some Ferengi wisdom:" @ferengi-wisdom-promise))
  (Thread/sleep 100)
  (deliver ferengi-wisdom-promise "Whisper your way to success."))

;;using what we now know to make a queue

;;start with a macro to avoid repetative sleeping
(defmacro wait
  "Sleep `timeout` seconds before eval body"
  [timeout & body]
  `(do (Thread/sleep ~timeout) ~@body))

(time (let [saying3 (promise)]
        (future (deliver saying3 (wait 100 "Cheerio!")))
        @(let [saying2 (promise)]
           (future (deliver saying2 (wait 400 "Pip pip!")))
           @(let [saying1 (promise)]
              (future (deliver saying1 (wait 200 "'Ello, gov'na!")))
              (println @saying1)
              saying1)
           (println @saying2)
           saying2)
        (println @saying3)
        saying3))

(defmacro enqueue
     ([q concurrent-promise-name concurrent serialized]
            `(let [~concurrent-promise-name (promise)]
                (future (deliver ~concurrent-promise-name ~concurrent))
                       (deref ~q)
                ~serialized
                ~concurrent-promise-name))
     ([concurrent-promise-name concurrent serialized]
        `(enqueue (future) ~concurrent-promise-name ~concurrent ~serialized)))

(time @(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
           (enqueue saying (wait 400 "Pip pip!") (println @saying))
           (enqueue saying (wait 100 "Cheerio!") (println @saying))))

;;TODO Exercises -- they look useful