(ns falx.mouse
  (:require [clj-gdx :as gdx]
            [falx.point :as pt]))

(defn current
  ([]
   {::point (:point @gdx/mouse-state)})
  ([m]
   {::point (:point @gdx/mouse-state)}))

(defn in?
  ([m rect]
   (pt/in? (::point m pt/id) rect))
  ([m x y w h]
   (pt/in? (::point m pt/id) x y w h)))