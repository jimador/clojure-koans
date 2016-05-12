(ns playground.list-comprehensions)

;;defs
(def nums (range 1 4))
(def letters (->> (range (int \a) (inc (int \c)))
                  (map char)))
(def blacklisted #{\b})

;; for macro
(for [number nums] (* number 2))

;; this is equivalent to map here
(map #(* % 2) nums)

;;2 lists -- for each COMBINATION of number and letter
(for [num nums
      letter letters]
  (apply str [num letter]))


;;with map -- a lot uglier
(mapcat
  (fn [number]
    (map
      (fn [letter]
        (str number letter))
      letters
      )
    ) nums)

;;for when
(for [thing1 nums
      thing2 letters
      thing3 (range 10)
      :when (and (= thing1 thing3)
                 (= thing2 :b))]
  [thing1 thing2 thing3])

(for [letter1 letters
      letter2 letters
      :when (and (not (blacklisted letter1))
                 (not (blacklisted letter2)))]
  (str letter1 letter2))