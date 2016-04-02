(ns falx.element)

(defn sprite
  [s rect]
  {:type :element/sprite
   :sprite s
   :rect rect})

(defn backing
  [rect]
  {:type :element/backing
   :rect rect})

(defn actor
  [id rect]
  {:type :element/actor
   :id id
   :rect rect})

(defn box
  [rect]
  {:type :element/box
   :rect rect})

(defn highlighted-box
  [rect]
  {:type :element/highlighted-box
   :rect rect})

(defn stat-label
  [id stat rect]
  {:type :element/stat-label
   :id id
   :stat stat
   :rect rect})

(defn viewport
  [camera rect]
  {:type :element/viewport
   :camera camera
   :rect rect})

(defn many
  [coll]
  {:type :element/many
   :coll coll})

(defn on-hover
  ([not-hovering hovering rect]
   {:type :element/on-hover
    :rect rect
    :hovering hovering
    :not-hovering not-hovering}))