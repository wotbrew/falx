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
  [rect]
  (let [[x y w h] rect
        hw (int (/ w 2))
        hh (int (/ h 2))
        gdx-cam @gdx-cam]

    (cam/set-size! gdx-cam w h)
    (cam/set-pos! gdx-cam (+ x hw) (+ y hh))))

(defmacro view
  [rect & body]
  `(gdx/with-cam
     @gdx-cam
     (sync! ~rect)
     ~@body))