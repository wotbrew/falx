(ns falx.game.attack
  (:require [falx.game :as game]
            [falx.game.focus :as focus]
            [falx.game.selection :as selection]
            [falx.thing :as thing]
            [falx.location :as location]
            [falx.game.move :as move]))

(defn should-attack?
  [defender attacker]
  (not= (:faction defender) (:faction attacker)))

(defn can-melee-attack?
  [defender attacker]
  (thing/adjacent? defender attacker))

(defn melee-attack
  [game defender attacker]
  (do
    (prn (:id attacker) "attacks" (:id defender))
    game))

(defn try-move-to-attack
  [game defender attacker]
  (game/add-thing game
    (move/move-adjacent-to attacker defender)))

(defn attack
  [game defender attacker]
  (if-not (should-attack? defender attacker)
    game
    (cond
      (can-melee-attack? defender attacker)
      (melee-attack game defender attacker)


      :else (try-move-to-attack game defender attacker))))

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