(ns falx.main
  (:gen-class))

(defn -main
  [& args]
  (require 'falx.core)
  ((ns-resolve 'falx.core 'init!)))