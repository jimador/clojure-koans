(ns playground.word-counter)

(def articles #{"a" "an" "the"})

(defn- weighted-count [count total]
  (.doubleValue (+ count (* count (/ count total)))))

(defn- weighted-list [coll total]
  (map (fn [x] (weighted-count (second x) total)) coll))

(defn- get-top-freqs [words]
  (->> words
       frequencies
       (sort-by val)
       reverse
       (take 3)
       ))

(defn- get-book [url]
  (-> url
      (slurp)
      (clojure.string/split-lines)))

(defn- parse-text [text]
  (->> text
       (str)
       (re-seq #"[\w|']+")
       (map #(clojure.string/lower-case %))
       (remove articles)
       (get-top-freqs)))

(defn- parse-book [coll]
    (let [title (first coll)
          text (rest coll)
          top-words (parse-text text)
          word-count (count text)
          word-freq (weighted-list top-words word-count)]
      {
       :title title
       :top-words top-words
       :word-count word-count
       :word-freq word-freq
       }
      ))

(defn- word-freq-parser [url]
  (-> url
      (get-book)
      (parse-book)))

(defn parse-all-books [coll]
  (map #(word-freq-parser %) coll))