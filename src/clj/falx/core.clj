(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.game-state :as gs]
            [falx.input :as input]
            [falx.util :as util]))

(def temp
  (let [p (gs/generate-eid)]
    (-> (gs/game-state)
        (gs/command
          {:command/type :command.type/spawn
           :eid p
           :pos (gs/->Cell :limbo [3 4])
           :template {:entity/type :entity.type/party
                      :entity/layer :entity.layer/party}}

          {:command/type :command.type/spawn
           :template {:entity/type :entity.type/creature
                      :creature/party p}}))))

(gdx/on-tick render
  [tick]
  (let [input (input/gdx-input tick)]
    (gdx/draw! (util/pprint-str input) [0 0 640 480])))