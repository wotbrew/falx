(ns falx.goal)

(defn move
  [cell]
  {:type :goal.type/move
   :removes #{:goal.type/move
              :goal.type/find-path
              :goal.type/walk-path}
   :cell cell})

(defn find-path
  [cell]
  {:type :goal.type/find-path
   :removes #{:goal.type/find-path
              :goal.type/walk-path}
   :cell cell})

(defn walk-path
  [path]
  {:type :goal.type/walk-path
   :removes #{:goal.type/walk-path
              :goal.type/find-path}
   :path path})