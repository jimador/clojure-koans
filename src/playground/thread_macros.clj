(ns playground.thread-macros)


;; -> Thread first
;;(set
;;  (clojure.string/split-lines
;;    (slurp "https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english-usa.txt")))

(def common-words
  (-> (slurp "https://raw.githubusercontent.com/first20hours/google-10000-english/master/google-10000-english-usa.txt")
      (clojure.string/split-lines)
      set))
(def text (slurp "http://www.clearwhitelight.org/hitch/hhgttg.txt"))

;; ->> Thread last
;;(first
;;  (reverse
;;    (sort-by
;;      val
;;      (frequencies
;;        (remove common-words
;;                (map (fn* [p1__1956#]
;;                       (clojure.string/lower-case p1__1956#))
;;                     (re-seq #"[\w|']+" text)))))))

(->> text
            (re-seq #"[\w|']+")
            #_(map #(clojure.string/lower-case %))
            #_(remove common-words)
            frequencies
            #_(sort-by val)
            #_reverse
            first)

;; #_ comment macro