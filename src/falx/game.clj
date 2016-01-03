(ns falx.game
  "The game datastructure and core functions on it are defined here.

  The game contains a
   `:world, :world-camera`, ..."
  (:require [falx.world :as world]
            [clj-gdx :as gdx]
            [falx.react :as react]
            [falx.input :as input]))

(def default
  {:world        world/default
   :world-camera gdx/default-camera
   :delta 0.0
   :input input/default})

(defonce ^:private reactions (atom {}))

(defn defreaction
  [event-type key f]
  (swap! reactions react/register event-type key f))

(defn react
  [game event]
  (react/react @reactions event game))

(defn publish-event
  "Publishes a new event that can be reacted to by the game.
  Returns a new game with the event in the `:events` coll"
  [game event]
  (-> (update game :events (fnil conj []) event)
      (react event)))

(defn publish-events
  [game coll]
  (reduce publish-event game coll))

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
     (-> (assoc game :world world)
         (publish-events events))))
  ([game f & args]
    (update-world game #(apply f % args))))

(defn add-thing
  "Like `(update-world game world/add-thing thing)`."
  [game thing]
  (update-world game world/add-thing thing))

(defn add-things
  "Like `(update-world game world/add-things coll)`."
  [game coll]
  (update-world game world/add-things coll))

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

(defn input-modified?
  "Is the input modifier key/button down?"
  [game]
  (input/modified? (:input game)))

(defn frame
  "Processes a single frame of the game, taking in the input and delta since last frame.
  Returns the new game"
  [game input delta]
  (let [events (input/get-input-events input)]
    (-> (assoc game :input input :delta delta)
        (publish-events events)
        (publish-event {:type :event.game/frame
                        :input input
                        :delta delta}))))