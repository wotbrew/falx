(ns falx.frame
  (:require [falx.game :as g]
            [falx.game-state :as gs]))

(defn game
  [frame]
  (g/game (:game frame)))

(defn mouse-loc
  [frame]
  (-> frame :tick :mouse-loc))

(defn config
  [frame]
  (-> frame :tick :config))

(defn screen-size
  [frame]
  (-> frame config :size))

(defn button-hit?
  [frame btn]
  (-> frame :tick :buttons-hit (contains? btn)))

(defn key-hit?
  [frame key]
  (-> frame :tick :keys-hit (contains? key)))

(defn key-down?
  [frame key]
  (-> frame :tick :keys-down (contains? key)))

(defn contains-loc?
  ([x y w h loc]
   (let [[x2 y2] loc]
     (contains-loc? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and (<= x x2 (+ x w -1))
        (<= y y2 (+ y h -1)))))

(defn mouse-in?
  [frame x y w h]
  (contains-loc? x y w h (mouse-loc frame)))

(defn clicked?
  [frame x y w h]
  (and (mouse-in? frame x y w h)
       (let [btn (gs/click-button (:state frame))]
         (button-hit? frame btn))))

(defn alt-clicked?
  [frame x y w h]
  (and (mouse-in? frame x y w h)
       (let [btn (gs/click-button (:state frame))]
         (button-hit? frame btn))))