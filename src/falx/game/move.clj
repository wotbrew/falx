(ns falx.game.move
  (:require [clojure.tools.logging :refer [debug]]
            [falx.game :as game]
            [falx.game.select :as game-select]
            [falx.actor :as actor]
            [falx.world :as world]
            [falx.creature :as creature]
            [clojure.core.async :as async]))

(defn can-move?
  [world actor cell]
  (let [acoll (world/get-at world cell)]
    (not (actor/some-obstructs? actor acoll))))

(defn move-selected
  [world cell]
  (->> (game-select/get-selected world)
       (filter #(can-move? world % cell))
       (reduce #(world/update-actor %1 (:id %2) creature/move cell) world)))

(defn ask-selected-to-move!
  [game cell]
  (game/update-world! game move-selected cell)
  {:type :move.event/selected-asked-to-move
   :cell cell})

(defn work-on-goal
  [creature goal]
  (let [c (async/chan)]
    (println "Woot!")
    (async/close! c)
    c))

(defn install!
  [game]
  (debug "Installing move module")
  (-> game
      (game/install-event-fn
        [:ui.event/world-clicked :left]
        #(ask-selected-to-move! game (:cell %)))
      (game/install-event-async-fn
        [:creature.event/goal-given :goal.type/move]
        #(work-on-goal (:creature %) (:goal %)))))