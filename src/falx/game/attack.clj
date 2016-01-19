(ns falx.game.attack
  (:require [falx.game :as game]
            [falx.game.focus :as focus]
            [falx.game.selection :as selection]
            [falx.thing :as thing]
            [falx.location :as location]
            [falx.game.move :as move]
            [falx.event :as event]
            [clojure.core.async :as async]
            [falx.state :as state]
            [falx.game.goal :as goal]))

(defn should-attack?
  [defender attacker]
  (not= (:faction defender) (:faction attacker)))

;; ===
;; MELEE

(defn melee-attack-goal
  [thing]
  {:type :goal/melee-attack
   :thing thing})

(defn can-melee-attack?
  [defender attacker]
  (thing/adjacent? defender attacker))

(defn melee-attack
  [game defender-id attacker-id]
  (let [defender (game/get-thing game defender-id)
        attacker (game/get-thing game attacker-id)]
    (if (can-melee-attack? defender attacker)
      (do
        (prn (:id attacker) "attacks" (:id defender))
        game)
      game)))

(defn melee-attack-for-goal
  [game defender-id attacker-id goal]
  (let [attacker (game/get-thing game attacker-id)]
    (if (goal/has? attacker goal)
      (melee-attack game defender-id attacker-id)
      game)))

(def melee-attack-wait-ms
  500)

(event/defhandler-async
  [:event.thing/goal-changed :goal/melee-attack]
  ::melee-attack
  (fn
    [{:keys [thing goal goal-id]}]
    (let [id (:id thing)
          target (:thing goal)
          target-id (:id target)]
      (async/go-loop
        []
        (let [game (state/get-game)
              attacker (game/get-thing game id)
              defender (game/get-thing game target-id)]
          (cond
            (not (goal/has-exact? attacker goal goal-id)) nil
            (not (should-attack? defender attacker)) nil
            (can-melee-attack? defender attacker) (state/update-game! #(-> %
                                                                           (melee-attack-for-goal target-id id goal)
                                                                           (goal/complete goal)))
            :else (do (async/<! (async/timeout melee-attack-wait-ms))
                      (recur))))))))

;; INPUT

(defn try-move-to-attack
  [game defender attacker]
  (let [goal (-> (move/goto-thing-goal defender)
                 (goal/then (melee-attack-goal defender)))]
    (game/add-thing game (goal/give attacker goal))))

(defn attack
  [game defender attacker]
  (if-not (should-attack? defender attacker)
    game
    (try-move-to-attack game defender attacker)))

(game/defreaction
  [:event.action :action.hit/attack]
  ::attack-hit
  (fn [game _]
    (let [world (:world game)
          focused (focus/get-creature game)
          selected (selection/get-selected world)]
      (if (and focused selected)
        (reduce #(attack %1 focused %2) game selected)
        game))))