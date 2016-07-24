(ns falx.mouse
  (:require [clj-gdx :as gdx]
            [falx.point :as pt]))

(defn current
  ([]
   (let [ms @gdx/mouse-state
         hit (:hit ms #{})]
     {::point (:point ms)
      ::clicked? (contains? hit :left)
      ::alt-clicked? (contains? hit :right)}))
  ([m]
    (current)))

(defn in?
  ([m rect]
   (pt/in? (::point m pt/id) rect))
  ([m x y w h]
   (pt/in? (::point m pt/id) x y w h)))

(defn clicked-in?
  ([m rect]
    (and (::clicked? m) (in? m rect))))

(defn alt-clicked-in?
  ([m rect]
   (and (::alt-clicked? m) (in? m rect))))

(defn size
  [m]
  [32 32])

(defn rect
  [m]
  (pt/->rect (::point m pt/id) (size m)))