(ns falx.init
  (:require [clojure.tools.logging :refer [info]]
            [falx.state :as state]
            [falx.thing :as thing]
            [falx.game :as game]
            [falx.thing.creature :as creature]
            [falx.thing.floor :as floor]
            [falx.thing.wall :as wall]
            [falx.rect :as rect]
            [falx.point :as point]
            [falx.location :as location]))

(def namespaces
  '[falx.game.describe
    falx.game.selection
    falx.game.goal
    falx.game.focus
    falx.game.camera
    falx.game.time
    falx.game.move
    falx.game.path
    falx.game.solid
    falx.game.attack

    falx.thing.creature

    falx.draw.game
    falx.draw.world
    falx.draw.thing

    falx.ai.move])

(defn fillout
  [game template points]
  (game/add-things
    game
    (map thing/put
         (take (count points) (thing/fresh-thing-seq template))
         (map (partial location/cell :testing) points))))

(defn init!
  []
  (info "Initializing game")
  (state/update-game! (constantly game/default))
  (info "Loading modules")
  (run! (fn [ns]
          (info "Loading core module" ns)
          (require [ns])) namespaces)
  (let [game
        (-> game/default
            (fillout
              floor/template
              (rect/get-points 0 0 16 16))
            (fillout
              wall/template
              (rect/get-edge-points 0 0 16 16))
            (fillout
              wall/template
              (take 4 (point/line-down 7 0)))
            (game/put-thing
              (merge
                (thing/thing "fred" creature/human)
                {:player? true})
              (location/cell
                :testing
                [3 4]))
            (game/put-thing
              (merge
                (thing/thing "ethel" creature/human)
                {:player? true
                 :gender :female})
              (location/cell
                :testing
                [6 6]))
            (game/put-thing
              (thing/thing "gobbo" creature/goblin-template)
              (location/cell
                :testing
                [9 9])))]
    (state/update-game! (constantly game)))
  (info "Initialized game"))

(comment
  (init!))