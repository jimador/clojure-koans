(ns brave-and-true.core-functions-ch4)

;;Sequence implement first rest and cons
(def foo [1 2 3])
(first foo)
(rest foo)
(cons 4 foo)

;;use map to combine function!
(def sum #(reduce + %))
(def avg #(/ (sum %) (count %)))
(defn stats
  [numbers]
  (map #(% numbers) [sum count avg]))

(stats [1 2 3])

;;a Map can be treated as a seqence of vectors
;;here we are using reduce to update the values in a map
(reduce
  (fn [new-map [k v]]
          (assoc new-map k (inc v)))
        {}
        {:max 30 :min 1})

;;here we are using reduce to filter a map base on it's values
(reduce
  (fn [new-map [k v]]
    (if (> v 4)
      (assoc new-map k v)
      new-map))
  {}
  {:human 4.1 :critter 3.9}
  )

(comment "More common operations"
         (take)
         (drop)
         (take-while)
         (drop-while)
         (filter)
         (some) "<- this is interesting, use to determine if a collection contains
                    some truthy value for a given predicate function"
         (sort)
         (sort-by #() data)
         (concat))

;;lazy-seq
(def vampire-database
  {0 {:makes-blood-puns? false, :has-pulse? true  :name "McFishwich"}
   1 {:makes-blood-puns? false, :has-pulse? true  :name "McMackson"}
   2 {:makes-blood-puns? true,  :has-pulse? false :name "Damon Salvatore"}
   3 {:makes-blood-puns? true,  :has-pulse? true  :name "Mickey Mouse"}})

(defn vampire-related-details
  [social-security-number]
  (Thread/sleep 1000)
  (get vampire-database social-security-number))

(defn vampire?
  [record]
  (and (:makes-blood-puns? record)
       (not (:has-pulse? record))
       record))

(defn identify-vampire
  [social-security-numbers]
  (first (filter vampire?
                 (map vampire-related-details social-security-numbers))))

(time (vampire-related-details 0))

;;map is lazy too!
(time (map vampire-related-details (range 0 1000000)))

;;repetition
(concat
  (take 3 (repeat "boo")) ["who"])
(take 3 (repeatedly #(rand-int 10)))

;;into --  convert the return data structure back into the original value
;;works with all data structure and converts between them
;;also into can have inital values inside it
;;great at taking 2 collections and shoving them together
(map identity {:sunlight-reaction "Glitter!"})
;;=> ([:sunlight-reaction "Glitter!"]) <-- NOOOO

(into {} (map identity {:sunlight-reaction "Glitter!"}))
;;=> {:sunlight-reaction "Glitter!"}

(into #{} (map identity [:garlic-clove :garlic-clove]))
;;=> #{:garlic-clove}

;;conj -- like into, but it takes a rest parameter and into a seqable data structure
(conj [0] [1])
; => [0 [1]] -- OOPS

(into [0] [1])
; => [0 1]

(conj [0] 1)
; => [0 1]

(comment (apply fn () data-structure)
         "apply will explode the elements of a collection and pass them to the
         fn one at a time")

(comment (partial)
         "takes a function and any number of arguments. It then returns a new
         function. When you call the returned function, it calls the original
         function with the original arguments you supplied it along with the
         new arguments.")

(defn lousy-logger
  [log-level message]
  (condp = log-level
    :warn (clojure.string/lower-case message)
    :emergency (clojure.string/upper-case message)))

(def warn (partial lousy-logger :warn))

(warn "Red light ahead")
; => "red light ahead"

(comment (complement)
         "return inverse")