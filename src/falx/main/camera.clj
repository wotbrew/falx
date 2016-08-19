(ns falx.main.camera
  (:require [falx.user :as user]
            [falx.engine.point :as pt]
            [falx.engine.camera :as cam]
            [falx.engine.rect :as rect]))

(def fast-requested?
  (user/binding ::user/bind.mod))

(def speed-setting
  (user/setting ::user/setting.cam-speed))

(defn speed
  [input user delta]
  (let [base (speed-setting user)
        factor 500]
    (if (fast-requested? user input)
      (* factor base delta 2.0)
      (* factor base delta 1.0))))

(def left-requested?
  (user/binding ::user/bind.cam-left))

(def right-requested?
  (user/binding ::user/bind.cam-right))

(def down-requested?
  (user/binding ::user/bind.cam-down))

(def up-requested?
  (user/binding ::user/bind.cam-up))

(defn add
  [cam dir speed]
  (rect/shift cam (pt/scale dir speed)))

(defn handle
  [cam input user delta]
  (let [cam (or cam [0 0 800 600])
        speed (speed input user delta)]
    (cond-> cam
      (left-requested? user input) (add pt/left speed)
      (right-requested? user input) (add pt/right speed)
      (down-requested? user input) (add pt/down speed)
      (up-requested? user input) (add pt/up speed))))

(def ^:dynamic *viewing* nil)

(defmacro view
  [cam & body]
  `(let [cam# ~cam
         viewing# cam#]
     (if (= viewing# *viewing*)
       (do ~@body)
       (binding [*viewing* viewing#]
         (cam/view
           cam#
           ~@body)))))

(defn world-point
  [cam pt]
  (view cam (cam/world-point pt)))

(defn screen-point
  [cam pt]
  (view cam (cam/screen-point pt)))