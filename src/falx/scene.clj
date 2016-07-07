(ns falx.scene
  (:require [clj-gdx :as gdx]
            [falx.sprite :as sprite]
            [falx.geom :as g])
  (:refer-clojure :exclude [compile]))

(defn node?
  [m]
  (and (map? m)
       (contains? m ::type)))

(defn- invoke
  [f]
  (f))

(defmulti drawfn* (fn [m] (::type m)))

(defmethod drawfn* :default
  [m]
  (fn []))

(defn drawfn
  [m]
  (drawfn* m))

(defn draw!
  [m]
  ((drawfn m)))

(defn text
  [m]
  (assoc m ::type :falx.scene.type/text))

(defmethod drawfn* :falx.scene.type/text
  [m]
  (let [{::keys [text font geom context]
         :or {text "nil"
              font gdx/default-font}} m
        x (int (::g/x geom 0))
        y (int (::g/y geom 0))
        w (int (::g/w geom 32))]
    (fn []
      (gdx/draw-string! text x y w context font))))

(defn sprite
  [m]
  (assoc m ::type :falx.scene.type/sprite))

(defmethod drawfn* :falx.scene.type/sprite
  [m]
  (let [{::keys [sprite geom context]
         :or {sprite sprite/pixel}} m
        x (int (::g/x geom 0))
        y (int (::g/y geom 0))
        w (int (::g/w geom 32))
        h (int (::g/h geom 32))]
    (fn []
      (gdx/draw-sprite! sprite x y w h context))))

(defn box
  [m]
  (assoc m ::type :falx.scene.type/box))

(defmethod drawfn* :falx.scene.type/box
  [m]
  (let [{::keys [geom context thickness]
         :or {thickness 1}} m
        x (int (::g/x geom 0))
        y (int (::g/y geom 0))
        w (int (::g/w geom 32))
        h (int (::g/h geom 32))
        t thickness
        b (+ y h (- t))
        r (+ x w (- t))]
    (fn []
      (gdx/draw-sprite! sprite/pixel x y w t context)
      (gdx/draw-sprite! sprite/pixel x y t h context)
      (gdx/draw-sprite! sprite/pixel x b w t context)
      (gdx/draw-sprite! sprite/pixel r y t h context))))

(defn tree
  [m]
  (assoc m ::type :falx.scene.type/tree))

(defmethod drawfn* :falx.scene.type/tree
  [m]
  (let [f (fn ! [acc v] (cond
                          (node? v) (conj acc (drawfn v))
                          (map? v) (reduce-kv (fn [acc _ v] (! acc v)) acc v)
                          (coll? v) (reduce ! acc v)
                          :else acc))
        fs
        (reduce-kv
          (fn [acc _ v] (f acc v))
          []
          m)]
    (fn []
      (run! invoke fs))))