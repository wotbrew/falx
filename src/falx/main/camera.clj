(ns falx.main.camera
  (:require [falx.user :as user]
            [falx.engine.point :as pt]
            [falx.engine.camera :as cam]))

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
  (pt/add cam (pt/scale dir speed)))

(defn handle
  [cam input user delta]
  (let [cam (or cam [0 0])
        speed (speed input user delta)]
    (cond-> cam
      (left-requested? user input) (add pt/left speed)
      (right-requested? user input) (add pt/right speed)
      (down-requested? user input) (add pt/down speed)
      (up-requested? user input) (add pt/up speed))))

(defmacro view
  [cam size & body]
  `(let [[cx# cy#] ~cam
        [w# h#] ~size]
    (cam/view
      [cx# cy# w# h#]
      ~@body)))