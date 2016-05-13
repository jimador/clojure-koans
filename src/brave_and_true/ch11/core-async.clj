(ns brave-and-true.ch11.core-async
  (:require [clojure.core.async
             :as a
             :refer :all])
  (:gen-class))

(def echo-chan (chan))
(go (println (<! echo-chan)))
(>!! echo-chan "ketchup")