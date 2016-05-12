(ns brave-and-true.functional-programming)

;;Composing functions
;;note -- * then inc
((comp inc *) 2 3)

(def character
  {:name "Smooches McCutes"
   :attributes {:intelligence 10
                :strength 4
                :dexterity 5}})
;; :attributes -> intelligence *remember keywords are functions too!
(def c-int (comp :intelligence :attributes))
(def c-str (comp :strength :attributes))
(def c-dex (comp :dexterity :attributes))

(c-int character)

;;when one of the functions you want to compose needs to take
;;more than one argument
(defn spell-slots
  [char]
  (int (inc (/ (c-int char) 2))))

;;less noise with comp
(def spell-slots-comp (comp int inc #(/ % 2) c-int))

(= (spell-slots character)
   (spell-slots-comp character))

(comment (memoize)
         "clojure has a memoize function!")

