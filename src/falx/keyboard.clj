(ns falx.keyboard)

(defn key->string
  [key]
  (when (= 1 (count (name key)))
    (name key)))

(defn shift-pressed?
  [keyboard]
  (or (contains? (:pressed keyboard) :shift-left)
      (contains? (:pressed keyboard) :shift-right)))