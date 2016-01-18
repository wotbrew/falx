(ns falx.state
  (:require [falx.game :as game]
            [falx.event :as event]
            [clojure.tools.logging :refer [error]]))

(defonce ^:private game-state
  (agent game/default))

(defn get-game
  "Returns the current game state"
  []
  @game-state)

(defn update-game-async!
  ([f]
   (let [eventsv (promise)
         res (promise)]
     (send game-state
           (fn [game]
             (try
               (let [game (f game)
                     {:keys [events game]} (game/split-events game)]
                 (deliver eventsv events)
                 (deliver res game)
                 game)
               (catch Throwable e
                 (error "An error occurred updating game")
                 (deliver eventsv [])
                 (deliver res e)
                 game))))
     (run! event/publish! @eventsv)
     (delay
       (let [r @res]
         (if (instance? Throwable r)
           (throw r)
           r)))))
  ([f & args]
    (update-game-async! #(apply f % args))))

(defn update-game!
  "Applies the function `f` and any `args` to the game atomically. Mutating the current game state.
  Returns the updated game."
  ([f]
   @(update-game-async! f))
  ([f & args]
   (update-game! #(apply f % args))))

(defn update-world!
  "Applies the function `f` and any `args` to the world in the game, mutating the current game state.
  Returns the updated world."
  ([f]
   (:world (update-game! game/update-world f)))
  ([f & args]
   (update-world! #(apply f % args))))

(defn get-thing
  "Finds and returns the thing given by `id`, nil if it doesnt exist."
  [id]
  (game/get-thing @game-state id))

(defn add-thing!
  "Adds the thing to the global game state"
  [thing]
  (update-game! game/add-thing thing)
  nil)

(defn remove-thing!
  "Removes the thing from the global game state"
  [id]
  (update-game! game/remove-thing id)
  nil)

(defn update-thing!
  "Applies the function `f` and any `args` to the thing given by `id` in the game.
  Mutates the global game state.
  Returns the thing."
  ([id f]
   (-> (update-game! game/update-thing id f)
       (game/get-thing id)))
  ([id f & args]
   (update-thing! id #(apply f % args))))

(defn put-thing!
  "Puts the thing in the cell, modifies the global game state and returns the updated game."
  [thing cell]
  (update-game! game/put-thing thing cell))

(defmacro silent
  "Discards the result of body (can stop REPL crashing if game gets too big to print!)"
  [& body]
  `(do ~@body nil))


