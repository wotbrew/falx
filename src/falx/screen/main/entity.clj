(ns falx.screen.main.entity
  (:require [falx.entity :as entity]
            [falx.entity.creature :as cre]
            [falx.engine.draw :as d]
            [falx.sprite :as sprite]
            [falx.screen :as screen]))

(def selection-circle
   (d/recolor sprite/selection screen/green))

(defn creature!
  [e x y w h]
  (when (::cre/selected? e)
    (d/draw! selection-circle x y w h))
  (d/draw! sprite/human-male x y w h))

(defn unknown!
  [e x y w h]
  (d/draw! "?" x y w h))

(defn draw!
  [e w h]
  (let [[wx wy] (::entity/point e)
        x (* w (long wx))
        y (* h (long wy))]
    (case (::entity/type e)
      ::entity/type.creature (creature! e x y w h)
      (unknown! e x y w h))))