(ns falx.position)

(defn position
  ([point map]
    (position point map 0))
  ([point map index]
    {:point point
     :map map
     :index index}))