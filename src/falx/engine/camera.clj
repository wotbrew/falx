(ns falx.engine.camera
  "Defines the macro `view` which allows you to alter a section
  of screen space given by a rectangle.

  This gives you a camera!"
  (:require [falx.gdx :as gdx]
            [falx.gdx.camera :as cam]
            [falx.engine.point :as pt]))

(defonce gdx-cam
  (delay (cam/camera [800 600])))

(defn sync!
  [gdx-cam rect]
  (let [[x y w h] rect
        hw (int (/ w 2))
        hh (int (/ h 2))]

    (cam/set-size! gdx-cam w h)
    (cam/set-pos! gdx-cam (+ x hw) (+ y hh))))

(defn screen-point
  [pt]
  (cam/screen-pt @gdx-cam pt))

(defn world-point
  [pt]
  (cam/world-pt @gdx-cam pt))

(defmacro view
  [rect & body]
  `(let [gdx-cam# @gdx-cam]
     (sync! gdx-cam# ~rect)
     (gdx/with-cam
       gdx-cam#
       ~@body)))