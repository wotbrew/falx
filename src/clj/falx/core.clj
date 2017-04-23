(ns falx.core
  (:require [falx.gdx :as gdx]
            [falx.game-state :as gs]))

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
  )