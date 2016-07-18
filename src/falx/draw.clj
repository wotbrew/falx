(ns falx.draw
  (:require [falx.geom :as g]
            [gdx.color :as color]
            [clj-gdx :as gdx]
            [falx.sprite :as sprite])
  (:import (clojure.lang Sequential)))

(defprotocol IDraw
  (-drawfn [this geom context]))

(defn- invoke
  [f]
  (f))

(extend-protocol IDraw
  Object
  (-drawfn [this geom context]
    (-drawfn (str this) geom context))
  Sequential
  (-drawfn [this geom context]
    (let [fs (mapv #(-drawfn % geom context) this)]
      (fn []
        (run! invoke fs))))
  String
  (-drawfn [this geom context]
    (let [{::g/keys [x y w] :or {x 0 y 0 w 9999}} geom
          font (:font context gdx/default-font)]
      (fn []
        (gdx/draw-string! this x y w context font))))
  nil
  (-drawfn [this geom context]
    (-drawfn "nil" geom context)))

(defn drawfn
  ([drawable]
   (drawfn drawable (g/point)))
  ([drawable geom]
   (drawfn drawable geom {:color color/white
                          :font gdx/default-font}))
  ([drawable geom context]
   (-drawfn drawable geom context)))

(defn draw!
  [drawable]
  ((drawfn drawable)))

(defrecord Size [drawable size]
  IDraw
  (-drawfn [this geom context]
    (let [geom' (g/resize geom size)]
      (drawfn drawable geom' context))))

(defn size
  [drawable size]
  (->Size drawable size))

(defrecord Tint [drawable color]
  IDraw
  (-drawfn [this geom context]
    (let [context (merge-with color/mult context {:color (or color color/white)})]
      (drawfn drawable geom context))))

(defn tint
  [drawable color]
  (->Tint drawable color))

(defrecord At [drawable point]
  IDraw
  (-drawfn [this geom context]
    (let [geom' (g/put geom point)]
      (drawfn drawable geom' context))))

(defn at
  [drawable point]
  (->At drawable point))

(defrecord Relative [drawable point]
  IDraw
  (-drawfn [this geom context]
    (let [geom' (g/add geom (g/point point))]
      (drawfn drawable geom' context))))

(defn relative
  [drawable point]
  (->Relative drawable point))

(defn fit
  [drawable rect]
  (-> drawable
      (relative rect)
      (size rect)))

(defrecord Image [sprite]
  IDraw
  (-drawfn [this geom context]
    (let [{::g/keys [x y w h] :or {x 0 y 0 w 32 h 32}} geom]
      (fn []
        (gdx/draw-sprite! sprite x y w h context)))))

(defn img
  [sprite]
  (->Image sprite))

(defrecord Box []
  IDraw
  (-drawfn [this geom context]
    (let [{::g/keys [x y w h] :or {x 0 y 0 w 32 h 32}} geom
          thickness (:thickness context 1)
          x+w (+ x w (- thickness))
          y+h (+ y h (- thickness))]
      (fn []
        (gdx/draw-sprite! sprite/pixel x y w thickness context)
        (gdx/draw-sprite! sprite/pixel x+w y thickness h context)
        (gdx/draw-sprite! sprite/pixel x y thickness h context)
        (gdx/draw-sprite! sprite/pixel x y+h w thickness context)))))

(def box (->Box))

(defn hspacing
  [x coll]
  (let [f (fn [i v] (relative v {::g/x (* x i) ::g/y 0}))]
    (into [] (map-indexed f) coll)))

(defn vspacing
  [y coll]
  (let [f (fn [i v] (relative v {::g/x 0 ::g/y (* y i)}))]
    (into [] (map-indexed f) coll)))