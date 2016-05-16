(ns clojure-koans.runtime-polymorphism)

(defn hello
  ([] "Hello World!")
  ([a] (str "Hello, you silly " a "."))
  ([a & more] (str "Hello to this group: "
                   (apply str
                          (interpose ", " (cons a more)))
                   "!")))


  "Some functions can be used in different ways - with no arguments"
  (= "Hello World!" (hello))

  "With one argument"
  (= "Hello, you silly world." (hello "world"))

  "Or with many arguments"
  (= "Hello to this group: Peter, Paul, Mary!"
     (hello "Peter" "Paul" "Mary"))


  (defmulti diet (fn [x] (:eater x)))
  (defmethod diet :herbivore [a] (str (:name a) " eats veggies."))
  (defmethod diet :carnivore [a] (str (:name a) " eats animals."))
  (defmethod diet :default [a] (str "I don't know what " (:name a) " eats."))

  "Multimethods allow more complex dispatching"
  (= "Bambi eats veggies."
     (diet {:species "deer" :name "Bambi" :age 1 :eater :herbivore}))

  "Animals have different names"
  (= "Thumper eats veggies."
     (diet {:species "rabbit" :name "Thumper" :age 1 :eater :herbivore}))

  "Different methods are used depending on the dispatch function result"
  (= "Simba eats animals."
     (diet {:species "lion" :name "Simba" :age 1 :eater :carnivore}))

  "You may use a default method when no others match"
  (= "I don't know what Rich Hickey eats."
     (diet {:name "Rich Hickey"}))

  (defn to [name] {:part :name
           :fool name})
  (defmulti sing (fn [song] (:part song)))
  (defmethod sing :name [a] (str "Happy birtday dear " (:fool a)))
  (defmethod sing :default [a] "Happy birthday to you")

  (sing {:part :name
         :fool "Brian"})
  (sing {})

  (defn sing-to [name]
    (for [idx (range 4)]
      (if (== idx 2)
        (println (sing (to name)))
        (println (sing {})))
    ))

(sing-to "brian")

;; define hierarchy
(derive ::reptile ::creature)
(derive ::bird ::reptile)
(derive ::mammal ::creature)

(derive ::dog ::mammal)
(derive ::cat ::mammal)

(derive ::dragon ::reptile)
(derive ::parrot ::bird)

(def creatures
  [{:type ::reptile :name "Gozilla"}
   {:type ::bird :name "Tweety"}
   {:type ::dog :name "Pluto"}
   {:type ::cat :name "Sylvester"}])

(defn creature-speak [type]
  (let [name (name type)]
    (str "Another thing that got forgotten was the fact that against all probability a " name "
   had suddenly been called into existence several miles above the surface
       of an alien planet. And since this is not a naturally tenable position
       for a " name ", this poor innocent creature had very little time to come
       to terms with its identity as a " name " any more before the GC cycle took it's life.")))


(defmulti talk :type)
(defmethod talk ::dog [c] (println (str (:name c) ": bark! bark!")))
(defmethod talk ::bird [c] (println (str (:name c) ": chirp! chirp!")))
(defmethod talk ::creature [c] (println (creature-speak (:type c))))

(defn run-test2 []
  (doseq [creature creatures]
    (talk creature)))

(run-test2)

