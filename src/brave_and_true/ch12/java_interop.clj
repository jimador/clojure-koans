(ns brave-and-true.ch12.java-interop)

;;Syntax
(.toUpperCase "some string")
;;=> SOME STRING

(.indexOf "some string" "r")
;;=>7

;;Creating and Mutating Objects
(new String)
; => ""

(String.)
; => ""

(String. "To Davey Jones's Locker with ye hardies")
; => "To Davey Jones's Locker with ye hardies"

(java.util.Stack.)
; => []

(let [stack (java.util.Stack.)]
    (.push stack "Latest episode of Game of Thrones, ho!")
    stack)
; => ["Latest episode of Game of Thrones, ho!"]

;;doto -- a macro to execute multiple methods on the same object more succinctly
;;  doto returns the Object instead of the return value of the method(s)
(doto (java.util.Stack.)
  (.push "Latest episode of Game of Thrones, ho!")
  (.push "Whoops, I meant 'Land, ho!'"))
; => ["Latest episode of Game of Thrones, ho!" "Whoops, I meant 'Land, ho!'"]

;;imports
;; (import [java.util Date Stack]
;;         [java.net Proxy URI])


;;Resourc manipulation
;;spit -- writes
;;slurp -- reads
;;with-open -- a convienent macro that implicitly closes a resource at the end
(comment (with-open [todo-list-rdr (clojure.java.io/reader "/tmp/hercules-todo-list")]
           (println (first (line-seq todo-list-rdr)))))
; => - kill dat lion brov
;;reader is a handy utility that keeps us from having to read a resource in its
;;  entirety

