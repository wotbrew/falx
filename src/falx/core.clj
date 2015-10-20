(ns falx.core
  (:gen-class)
  (:require [clojure.tools.logging :refer [info error]]
            [falx.application :as app]))

(defn run-frame!
  [])

(defn -main
  [& args]
  (app/application #'run-frame!))
