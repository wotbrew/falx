(ns falx.frame
  (:require [falx.input :as input]
            [falx.gdx :as gdx]))

(defrecord Frame [app-state
                  input-state
                  delta-time
                  elapsed-time])

(defn frame-source
  "Returns a function that when invoked will yield the next frame.
  The application state is sampled over time via the given 'app-ref' which must be deref'able
  in order to discover the current falx.app.AppState value."
  [app-ref]
  (let [input-atom (atom nil)
        nanotime-atom (atom nil)
        elapsed-time-atom (atom 0)]
    (fn []
      (let [app-state @app-ref
            input-state (swap! input-atom input/input-state @(gdx/run (input/input-state)))
            delta-time (let [v (volatile! 0.0)]
                         (swap! nanotime-atom
                                (fn [t]
                                  (let [nt (System/nanoTime)]
                                    (if t
                                      (vreset! v (- nt t))
                                      nt))))
                         @v)
            elapsed-time (swap! elapsed-time-atom + delta-time)]

        (->Frame
          app-state
          input-state
          delta-time
          elapsed-time)))))