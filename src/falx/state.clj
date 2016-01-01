(ns falx.state
  (:require [falx.game :as game]
            [falx.event :as event]))

(def game (atom {}))

(defn update-game!
  ([f]
   (let [eventsv (volatile! nil)
         game
         (swap! game
                (fn [game]
                  (let [game (f game)
                        {:keys [events game]} (game/split-events game)]
                    (vreset! eventsv events)
                    game)))]
     (run! event/publish! @eventsv)
     game))
  ([f & args]
   (update-game! #(apply f % args))))

(defn update-world!
  ([f]
   (update-game! game/update-world f))
  ([f & args]
   (update-world! #(apply f % args))))