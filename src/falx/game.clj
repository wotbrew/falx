(ns falx.game
  "The game datastructure and core functions on it are defined here.

  The game contains a
   `:world`, ..."
  (:require [falx.world :as world]))

(defn publish-event
  "Publishes a new event that can be reacted to by the game.
  Returns a new game with the event in the `:events` coll"
  [game event]
  (update game :events (fnil conj []) event))

(defn split-events
  "Takes an events in the game, and yields a map
  {:events, :game}. Where the `:events`
   and :game will be the game without the events."
  [game]
  {:events (:events game)
   :game (dissoc game :events)})

(defn update-world
  "Applies the function `f` and any args to the world.
  Surfaces any world events to the game."
  ([game f]
   (let [world (f (:world game))
         {:keys [world events]} (world/split-events world)]
     (as->
       game game
       (assoc game :world world)
       (reduce publish-event game events))))
  ([game f & args]
    (update-world game #(apply f % args))))

(defn add-thing
  "Like `(update-world game add-thing thing)`."
  [game thing]
  (update-world game world/add-thing thing))

(defn get-thing
  "Gets a thing from the games world"
  [game id]
  (world/get-thing (:world game) id))

(defn remove-thing
  "Like `(update-world game world/remove-thing thing)`"
  [game id]
  (update-world game world/remove-thing id))

(defn update-thing
  "Like `(update-world game world/update-thing id f)`"
  ([game id f]
   (update-world game world/update-thing id f))
  ([game id f & args]
   (update-thing game id #(apply f % args))))