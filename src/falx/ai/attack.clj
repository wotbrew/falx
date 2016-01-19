(ns falx.ai.attack
  (:require [falx.game.attack :as attack]
            [falx.game :as game]
            [falx.game.goal :as goal]
            [falx.event :as event]
            [clojure.core.async :as async]
            [falx.state :as state]))

(defn melee-attack-for-goal
  [game defender-id attacker-id goal]
  (let [attacker (game/get-thing game attacker-id)]
    (if (goal/has? attacker goal)
      (attack/melee-attack game defender-id attacker-id)
      game)))

(event/defhandler-async
  [:event.thing/goal-changed :goal/melee-attack]
  ::melee-attack
  (fn
    [{:keys [thing goal goal-id]}]
    (async/go
      (let [id (:id thing)
            target (:thing goal)
            target-id (:id target)
            game (state/get-game)
            attacker (game/get-thing game id)
            defender (game/get-thing game target-id)]
        (cond
          (not (goal/has-exact? attacker goal goal-id)) nil
          (not (attack/should-attack? defender attacker)) nil
          (attack/can-melee-attack? defender attacker) (state/update-game-async!
                                                         #(-> %
                                                              ;;should check if can attack before completing?
                                                              (melee-attack-for-goal target-id id goal)
                                                              (goal/complete goal)))
          :else
          (state/update-game-async!`
            (fn [game]
              (if-some [defender (game/get-thing game target-id)]
                (game/update-thing game target-id attack/try-move-to-attack defender)
                game))))))))
