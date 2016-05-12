(ns brave-and-true.organization)

;;namespaces -- map to symbols
;;symbols -- datatypes w/in the clojure language (map, inc, etc)
;;refer -- gives you fine grained control over how you refer to objects
;;         in other namespaces. When you call refer, you can also pass it
;;         the filters :only, :exclude, and :rename. ex
;;         (clojure.core/refer 'cheese.taxonomy :only ['bries])
;;alias -- (clojure.core/alias 'taxonomy 'cheese.taxonomy)
;;a more succienct ex is (require '[the-divine-cheese-code.visualization.svg :as svg])
;;there are six references within ns
;;(:refer-clojure)
;;(:require) -- (:require the-divine-cheese-code.visualization.svg)) (notice, no '
;;              also, can have multiple in [thing.stuff :as do] [thing.more :as foo]
;;(:use) -- (:use [clojure.java browse io])
;;          equivalent to (use 'clojure.java.browse)
;;                        (use 'clojure.java.io)
;;(:import)
;;(:load)
;;(:gen-class)

