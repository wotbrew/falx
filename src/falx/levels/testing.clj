(ns falx.levels.testing
  (:require [falx.entity :as entity]
            [falx.rect :as rect]
            [falx.entities.wall :as wall]
            [falx.entities.floor :as floor]
            [falx.world :as world]))

(def level :testing)

(defn cell
  [point]
  (entity/cell level point))

(def terrain-key
  {\# wall/wall
   \space floor/floor})

(def tmap
  ["#################"
   "#      ####     #"
   "#               #"
   "#               #"
   "#      ####     #"
   "########  ###   #"
   "#               #"
   "#               #"
   "#               #"
   "#               #"
   "########  ###   #"
   "#      ####     #"
   "#               #"
   "#               #"
   "#      ####     #"
   "#################"])

(def terrain
  (->> tmap
       (map #(replace terrain-key %))
       (map-indexed (fn [y row]
                      (map-indexed
                        (fn [x entity]
                          (entity/put entity (cell [x y])))
                        row)))))

(def entities
  (flatten
    [terrain
     (entity/put
       {:type :entity/creature
        :id "fred"}
       (cell [1 1]))
     (entity/put
       {:type :entity/creature}
       (cell [2 2]))]))

(def world
  (delay
    (world/add-entities
      world/empty
      entities)))