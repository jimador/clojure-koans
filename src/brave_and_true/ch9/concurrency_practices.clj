(ns brave-and-true.ch9.concurrency-practices)

;;Reference cell problem -- 2 threads reading and writing to the same location
;;Clojure has a few tools to deal with this

;;future
(future (Thread/sleep 4000)
        (println "I print after 4 seconds"))
(println "I print now!")