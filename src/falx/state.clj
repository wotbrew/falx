(ns falx.state
  (:require [falx.event :as event]
            [falx.game :as game]
            [falx.world :as world]))

(def game
  (let [a (atom game/default)]
    (add-watch a :changed
               (fn [_ _ old new]
                 (when (not= (:world old) (:world new))
                   (event/publish-event! {:type  :world-changed}))))
    a))

(defn capture-events!
  [game v]
  (let [{:keys [screen world]} game
        events (concat (:events world)
                       (:events screen))]
    (if (seq events)
      (do
        (vreset! v events)
        (assoc game :screen (assoc screen :events [])
                    :world (assoc world :events [])))
      game)))

(defn run-frame!
  [input frame]
  (let [v (volatile! [])
        game (swap! game #(-> (game/run-frame % input frame)
                              (capture-events! v)))
        events @v]
    (when (seq events)
      (event/publish-events! events))
    game))

(defn get-entity
  [eid]
  (game/get-entity @game eid))

(defn update-entity!
  ([eid f]
   (-> (swap! game game/update-entity eid f)
       :world
       (world/get-entity eid)))
  ([eid f & args]
    (update-entity! eid #(apply f % args))))