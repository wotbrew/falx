(ns falx.game.attack
  (:require [falx.game :as game]
            [falx.game.focus :as focus]
            [falx.game.selection :as selection]
            [falx.thing :as thing]
            [falx.game.move :as move]
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

;; INPUT

(defn try-move-to-attack
  [attacker defender]
  (let [goal (-> (move/goto-thing-goal defender)
                 (goal/then (melee-attack-goal defender)))]
    (goal/give attacker goal)))

(game/defreaction
  [:event.action :action.hit/attack]
  ::attack-hit
  (fn [game _]
    (let [world (:world game)
          focused (focus/get-creature game)
          selected (selection/get-selected world)]
      (if (and focused selected)
        (game/add-things game (map #(try-move-to-attack % focused) selected))
        game))))