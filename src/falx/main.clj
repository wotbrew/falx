(ns falx.main
  (:require [falx.scene :as scene]
            [falx.debug :as debug]
            [falx.ui :as ui]
            [falx.input :as input]
            [falx.keyboard :as keyboard]
            [falx.sprite :as sprite]))

(def mouse
  (ui/at-mouse sprite/mouse-point 32 32))

(def scene*
  (scene/stack
    (scene/fit #'debug/table 400 96)
    mouse))

(def commands*
  {::command.exit (input/hit ::keyboard/key.esc)})

(def commands
  (into {} (map (juxt key (comp input/compile val))) commands*))

(defmulti check-command (fn [gs command] command))

(defmethod check-command :default
  [gs _]
  true)

(defmulti user-command (fn [gs k] k))

(defmethod user-command :default
  [gs _]
  gs)

(defn handle-user-input
  [gs]
  (reduce-kv (fn [gs k i]
               (if (and (input/check gs i)
                        (check-command gs k))
                 (user-command gs k)
                 gs)) gs commands))

(defn handle-all
  [gs rect]
  (handle-user-input gs))

(defmethod user-command ::command.exit
  [gs _]
  (assoc gs :falx/screen :falx.screen/menu))

(def scene
  (ui/behaviour
    scene*
    handle-all))