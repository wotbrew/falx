(ns falx.init
  (:require [clojure.tools.logging :refer [info]]
            [falx.state :as state]
            [falx.thing :as thing]
            [falx.game :as game]))

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

    falx.thing.creature

    falx.draw.world
    falx.draw.thing

    falx.ai.move])

(defn init!
  []
  (info "Initializing game")
  (state/update-game! (constantly game/default))
  (info "Loading modules")
  (run! (fn [ns]
          (info "Loading core module" ns)
          (require [ns])) namespaces)
  (state/put-thing!
    {:id "fred"
     :type :creature
     :solid? true}
    (thing/cell
      :testing
      [3 4]))
  (state/put-thing!
    {:id "bob"
     :type :creature
     :solid? true}
    (thing/cell
      :testing
      [6 6]))
  (info "Initialized game"))

(comment
  (init!))