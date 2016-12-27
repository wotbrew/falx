(ns falx.core
  (:require [falx.gdx :as gdx])
  (:import (com.badlogic.gdx.graphics Color)))

(def game-state-ref
  (atom {}))

(defn current-frame
  ([]
   (current-frame (gdx/current-tick)))
  ([tick]
   {:game @game-state-ref
    :tick tick}))

(defprotocol IScreenObject
  (-handle! [this frame x y w h]))

(extend-protocol IScreenObject
  Object
  (-handle! [this frame x y w h]
    (gdx/draw-in! this x y w h)))

(defprotocol IMeasure
  (measure [this w h]))

(extend-protocol IMeasure
  Object
  (measure [this w h]
    [w h])
  String
  (measure [this w h]
    (gdx/measure this w h)))

(defn contains-loc?
  ([x y w h loc]
   (let [[x2 y2] loc]
     (contains-loc? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and (<= x x2 (+ x w -1))
        (<= y y2 (+ y h -1)))))

(defn handle!
  ([obj frame]
   (let [[w h] (-> frame :tick :config :size)]
     (handle! obj frame 0 0 w h)))
  ([obj frame x y w h]
   (-handle! obj frame x y w h)))

(defn recolor
  [el color]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (gdx/with-color color
        (-handle! el frame x y w h)))))

(defn tint
  [el color]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (gdx/with-tint color
                     (-handle! el frame x y w h)))))

(defn switch-elem
  [f m]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (let [k (f frame x y w h)]
        (when-some [o (get m k)]
          (-handle! o frame x y w h))))))


(def nil-elem
  (reify IScreenObject
    (-handle! [this frame x y w h])))

(defn if-elem
  ([pred then]
   (if-elem pred then nil-elem))
  ([pred then else]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (if (pred frame x y w h)
         (-handle! then frame x y w h)
         (-handle! else frame x y w h))))))

(defn mouse-in?
  [frame x y w h]
  (contains-loc? x y w h (-> frame :tick :mouse-loc)))

(defn if-hovering
  ([then]
    (if-hovering then nil-elem))
  ([then else]
    (if-elem mouse-in?
      then else)))

(defn resize
  ([loc el]
   (let [[w h] loc]
     (resize w h el)))
  ([w h el]
   (reify IScreenObject
     (-handle! [this frame x y _ _]
       (-handle! el frame x y w h))
     IMeasure
     (measure [this _ _]
       [w h]))))

(defn stack
  ([] nil-elem)
  ([a] a)
  ([a b]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h))))
  ([a b c]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h)
       (-handle! c frame x y w h))))
  ([a b c d]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h)
       (-handle! c frame x y w h)
       (-handle! d frame x y w h))))
  ([a b c d & more]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h)
       (-handle! c frame x y w h)
       (-handle! d frame x y w h)
       (run! #(-handle! % frame x y w h) more)))))


(defn center-rect
  ([size rect]
   (let [[x2 y2 w2 h2] rect]
     (center-rect size x2 y2 w2 h2)))
  ([size x2 y2 w2 h2]
   (center-rect (nth size 0) (nth size 1) x2 y2 w2 h2))
  ([w1 h1 x2 y2 w2 h2]
   (let [nw (min w1 w2)
         nh (min h1 h2)
         wdiff (- w2 nw)
         hdiff (- h2 nh)]
     [(long (+ x2 (/ wdiff 2)))
      (long (+ y2 (/ hdiff 2)))
      nw
      nh])))

(defn draw-fancy-box!
  ([x y w h]
   (draw-fancy-box! x y w h 1))
  ([x y w h t]
   (let [x (float x)
         y (float y)
         w (float w)
         h (float h)
         t (float t)]
     (gdx/draw-pixel! x y w t)
     (gdx/draw-pixel! x y t h)
     (gdx/with-tint Color/DARK_GRAY
       (gdx/draw-pixel! x (+ y h (- t)) w t)
       (gdx/draw-pixel! (+ x w (- t)) y t h)))))

(defn fancy-box
  [n]
  (reify gdx/IDrawIn
    (draw-in! [this x y w h]
      (draw-fancy-box! x y w h n))))

(defn center
  ([el]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (let [[w2 h2] (measure el w h)
              [x y w h] (center-rect w2 h2 x y w h)]
          (-handle! el frame x y w h))))))

(defn translate
  ([x2 y2 el]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (-handle! el frame (+ x x2) (+ y y2) (- w x2 x2) (- h y2 y2))))))

(defn button
  [el]
  (let [st (stack (fancy-box 2) (center el))]
    (if-hovering
      (tint st Color/YELLOW)
      st)))

(defn rows
  [& els]
  (let [els (vec els)]
    (if (empty? els)
      nil-elem
      (reify IScreenObject
        (-handle! [this frame x y w h]
          (let [row-height (long (/ h (count els)))]
            (loop [row 0]
              (when (< row (count els))
                (handle! (els row) frame x (+ y (* row row-height)) w row-height)
                (recur (inc row))))))))))

(defn fixed-rows
  [row-height & els]
  (let [els (vec els)]
    (reify IScreenObject
      (-handle! [this frame x y w _]
        (loop [row 0]
          (when (< row (count els))
            (handle! (els row) frame x (+ y (* row row-height)) w row-height)
            (recur (inc row))))))))

(defn cols
  [& els]
  (let [els (vec els)]
    (if (empty? els)
      nil-elem
      (reify IScreenObject
        (-handle! [this frame x y w h]
          (let [col-width (long (/ w (count els)))]
            (loop [col 0]
              (when (< col (count els))
                (handle! (els col) frame  (+ x (* col col-width)) y col-width h)
                (recur (inc col))))))))))

(defn fixed-cols
  [col-width & els]
  (let [els (vec els)]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (loop [cols 0]
          (when (< cols (count els))
            (handle! (els cols) frame (+ x (* cols col-width)) y col-width h)
            (recur (inc cols))))))))

(gdx/on-tick tick
  [tick]
  (let [frame (current-frame tick)
        [w h] (:size (:config tick))]
    (handle!
      (translate
        32 32
        (rows
          (cols
            (button "fred")
            (center (resize 64 64 (button "bar")))
            (button "ethel"))
          (button "foo")))
      frame)))

(defn init!
  [])