(ns brave-and-true.ch8.writing-macros)

(defmacro infix
  "Use this macro when you pine for the notation of your childhood"
  [infixed]
  (list (second infixed) (first infixed) (last infixed)))

(defmacro infix-2
  "A little destructuring magicks"
  [[operand1 op operand2]]
  (list op operand1 operand2))

;;ugly macro
(comment (defmacro code-critic
           "Phrases are courtesy Hermes Conrad from Futurama"
           [bad good]
           (list 'do
                 (list 'println
                       "Great squid of Madrid, this is bad code:"
                       (list 'quote bad))
                 (list 'println
                       "Sweet gorilla of Manila, this is good code:"
                       (list 'quote good)))))

;;much better
(comment (defmacro code-critic-nicer
           "Phrases are courtesy Hermes Conrad from Futurama"
           [bad good]
           `(do (println "Great squid of Madrid, this is bad code:"
                         (quote ~bad))
                (println "Sweet gorilla of Manila, this is good code:"
                         (quote ~good)))))

;;let's remove some duplicate code
(defn criticize-code
  [criticism code]
  `(println ~criticism (quote ~code)))

(comment (defmacro code-critic
           [bad good]
           `(do ~(map #(apply criticize-code %)
                      [["Great squid of Madrid, this is bad code:" bad]
                       ["Sweet gorilla of Manila, this is good code:" good]])))

         (code-critic (1 + 1) (+ 1 1))
         ; => NullPointerException -- dang
         )

;;enter ~@ -- Unquote splicing Unquote splicing unwraps a seqable data
;; structure, placing its contents directly within the enclosing
;; syntax-quoted data structure.

(defmacro code-critic
  [{:keys [good bad]}]
  `(do ~@(map #(apply criticize-code %)
              [["Sweet lion of Zion, this is bad code:" bad]
               ["Great cow of Moscow, this is good code:" good]])))

;;GOTCHAS!

;;variable capture
(def message "Good job!")
(defmacro with-mischief
  [& stuff-to-do]
  (concat (list 'let ['message "Oh, big deal!"])
          stuff-to-do))

(with-mischief
  (println "Here's how I feel about that thing you did: " message))
; => Here's how I feel about that thing you did: Oh, big deal!
;;The macro overwrote the value of message.

;;Enter gynsym to the rescue -- use gensym to avoid variable capture issues
(defmacro without-mischief
  [& stuff-to-do]
  (let [macro-message (gensym 'message)]
    `(let [~macro-message "Oh, big deal!"]
       ~@stuff-to-do
       (println "I still need to say: " ~macro-message))))

(without-mischief
  (println "Here's how I feel about that thing you did: " message))
; => Here's how I feel about that thing you did:  Good job!
; => I still need to say:  Oh, big deal!

;;Double evaluation
(defmacro report
  [to-try]
  `(if ~to-try
     (println (quote ~to-try) "was successful:" ~to-try)
     (println (quote ~to-try) "was not successful:" ~to-try)))

;;Thread/sleep takes a number of milliseconds to sleep for
(report (do (Thread/sleep 1000) (+ 1 1)))
;;Here, the println will get called twice. Use a let to only evaluate to-try
;;once

(defmacro report
  [to-try]
  `(let [result# ~to-try]
     (if result#
       (println (quote ~to-try) "was successful:" result#)
       (println (quote ~to-try) "was not successful:" result#))))

