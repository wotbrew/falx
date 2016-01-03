(ns falx.game
  "The game datastructure and core functions on it are defined here.

  The game contains a
   `:world`, ..."
  (:require [falx.world :as world]))

(defn split-events
  "Takes an events in the game, and yields a map
  {:events, :game}. Where the `:events`
   and :game will be the game without the events."
  [game]
  (let [{:keys [world]} game
        {:keys [events world]} (world/split-events world)]
    {:events events
     :game (assoc game :world world)}))

(defn update-world
  "Applies the function `f` and any args to the world."
  ([game f]
    (update game :world f))
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