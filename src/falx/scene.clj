(ns falx.scene
  (:require [falx.rect :as rect]
            [falx.size :as size]
            [falx.scene.protocols :refer [INode ISized -size -layout] :as proto])
  (:import (clojure.lang Var)
           (falx.scene.protocols IWrapMany)))

(defn layout
  ([scene rect]
   (layout [] scene rect))
  ([result scene rect]
   (-layout scene result rect)))

(defn size*
  ([node rect]
   (if (satisfies? ISized node)
     (-size node rect))))

(defn size
  ([node rect]
   (if (satisfies? ISized node)
     (-size node rect)
     (rect/size rect)))
  ([node x y w h]
   (size node [x y w h])))

(extend-protocol INode
  Object
  (-layout [this result rect]
    (conj result [this rect]))
  Var
  (-layout [this result rect]
    (-layout (var-get this) result rect)))

(defn maxw
  [rect nodes]
  (let [r (transduce (comp
                       (keep #(size* % rect))
                       (map size/w))
                     (completing max)
                     -1 nodes)]
    (if (neg? r)
      (rect/w rect)
      r)))


(defn maxh
  [rect nodes]
  (let [r (transduce (comp
                       (keep #(size* % rect))
                       (map size/h))
                     (completing max)
                     -1 nodes)]
    (if (neg? r)
      (rect/h rect)
      r)))

(extend-protocol ISized
  falx.scene.protocols.IWrap
  (-size [this rect]
    (size (proto/-child this) rect))
  falx.scene.protocols.IWrapMany
  (-size [this rect]
    [(maxw rect (proto/-children this))
     (maxh rect (proto/-children this))]))

(defrecord Stack [nodes]
  proto/INode
  (-layout [this result rect]
    (reduce #(layout %1 %2 rect) result nodes))
  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->stack
  [nodes]
  (->Stack (vec nodes)))

(defn stack
  ([& nodes]
   (coll->stack nodes)))

(defrecord Pad [node left top right bottom]
  proto/INode
  (-layout [this result rect]
    (let [[x y w h] rect]
      (layout result node
              [(+ x left)
               (+ y top)
               (- w left right)
               (- h top bottom)])))
  proto/IWrap
  (-child [this]
    node))

(defn pad
  ([node padding]
   (pad node
        (:left padding 0)
        (:top padding 0)
        (:right padding 0)
        (:bottom padding 0)))
  ([node left top bottom right]
   (->Pad node left top bottom right)))

(defrecord At [node pt]
  proto/INode
  (-layout [this result rect]
    (layout result node (rect/shift rect pt)))
  proto/IWrap
  (-child [this]
    this))

(defn at
  "Returns a node offset by `x` and `y`."
  ([node pt]
   (->At node pt))
  ([node x y]
   (at node [x y])))

(defrecord AtRight [node pt]
  INode
  (-layout [this result rect]
    (let [[x y w h] rect
          [x2 y2] pt
          x (- (+ x w) x2)
          y (+ y2 y)]
      (layout result node [x y w h])))
  proto/IWrap
  (-child [this]
    node))

(defn at-right
  [node pt]
  (->AtRight node pt))

(defrecord Center [node size]
  INode
  (-layout [this result rect]
    (let [rect (size/center size rect)]
      (layout result node rect)))
  proto/IWrap
  (-child [this]
    node))

(defn center
  "Centers the node according to the given size."
  ([node size]
   (->Center node size))
  ([node w h]
   (->Center node [w h])))

(defrecord Fit [node size]
  INode
  (-layout [this result rect]
    (layout result node (rect/fit rect (proto/-size this rect))))
  proto/IWrap
  (-child [this]
    this)
  proto/ISized
  (-size [this rect]
    (or size (falx.scene/size node rect))))

(defn fit
  "Fits the node to the size (if the requested size is larger).
  If size isn't supplied, the size of the children is used"
  ([node]
   (->Fit node nil))
  ([node size]
   (->Fit node size))
  ([node w h]
   (fit node [w h])))

(defrecord FitWidth [node w]
  INode
  (-layout [this result rect]
    (layout result node (rect/fitw rect w)))
  proto/IWrap
  (-child [this]
    node)
  proto/ISized
  (-size [this [x y _ h]]
    [w h]))

(defn fitw
  "Fits the node to the size (if the requested size is larger)"
  ([node w]
   (->FitWidth node w)))

(defrecord FitHeight [node h]
  INode
  (-layout [this result rect]
    (layout result node (rect/fith rect h)))
  proto/IWrap
  (-child [this]
    node)
  proto/ISized
  (-size [this [x y w _]]
    [w h]))

(defn fith
  "Fits the node to the size (if the requested size is larger)"
  ([node h]
   (->FitHeight node h)))

(defrecord Rows [nodes]
  INode
  (-layout [this result rect]
    (if (empty? nodes)
      result
      (let [[x y rw rh] rect
            n (count nodes)
            ih (long (/ rh n))
            ctr (volatile! 0)]
        (reduce-kv
          (fn [result i node]
            (let [[w h] (size node rect)
                  h (min h ih)
                  y (+ y @ctr)
                  offset (vswap! ctr + h)]
              (if (= i (dec n))
                (layout result node [x y w (max h (- rh (- offset h)))])
                (layout result node [x y w h]))))
          result nodes))))

  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->rows
  ([nodes]
   (->Rows (vec nodes))))

(defn rows
  [& nodes]
  (coll->rows nodes))

(defrecord FixedRows [h nodes]
  INode
  (-layout [this result rect]
    (let [[x y w] rect]
      (reduce-kv
        (fn [result i node]
          (layout result node [x (+ y (* h i)) w h]))
        result nodes)))
  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->frows
  ([h nodes]
   (->FixedRows h (vec nodes))))

(defn frows
  [h & nodes]
  (coll->frows h nodes))

(defrecord Cols [nodes]
  INode
  (-layout [this result rect]
    (if (empty? nodes)
      result
      (let [[x y rw h] rect
            n (count nodes)
            iw (long (/ rw n))
            ctr (volatile! 0)]
        (reduce-kv
          (fn [result i node]
            (let [[w h] (size node rect)
                  w (min w iw)
                  x (+ x @ctr)
                  offset (vswap! ctr + w)]
              (if (= i (dec n))
                (layout result node [x y (max w (- rw (- offset w))) h])
                (layout result node [x y w h]))))
          result nodes))))
  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->cols
  ([nodes]
   (->Cols (vec nodes))))

(defn cols
  [& nodes]
  (coll->cols nodes))

(defrecord FixedCols [w nodes]
  INode
  (-layout [this result rect]
    (let [[x y _ h] rect]
      (reduce-kv
        (fn [result i node]
          (layout result node [(+ x (* i w)) y w h]))
        result nodes)))
  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->fcols
  ([w nodes]
   (->FixedCols w (vec nodes))))

(defn fcols
  [w & nodes]
  (coll->fcols w nodes))

(defrecord HItems [size nodes]
  INode
  (-layout [this result rect]
    (let [[w h] size
          [x y w2 h2] rect
          cols (long (/ w2 w))
          rows (long (/ h2 h))]
      (reduce-kv
        (fn [result i node]
          (let [col (mod i cols)
                row (mod (long (/ i cols)) rows)]
            (layout result
                    node
                    [(+ x (* w col))
                     (+ y (* h row))
                     w
                     h])))
        result
        nodes)))
  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->hitems
  ([size nodes]
   (->HItems size (vec nodes))))

(defn hitems
  [size & nodes]
  (coll->hitems size nodes))

(def coll->items coll->hitems)

(def items hitems)

(defrecord VItems [size nodes]
  INode
  (-layout [this result rect]
    (let [[w h] size
          [x y w2 h2] rect
          cols (long (/ w2 w))
          rows (long (/ h2 h))]
      (reduce-kv
        (fn [result i node]
          (let [row (mod i rows)
                col (mod (long (/ i rows)) cols)]
            (layout result
                    node
                    [(+ x (* w col))
                     (+ y (* h row))
                     w
                     h])))
        result
        nodes)))
  proto/IWrapMany
  (-children [this]
    nodes))

(defn coll->vitems
  ([size nodes]
   (->VItems size (vec nodes))))

(defn hitems
  [size & nodes]
  (coll->hitems size nodes))

(defn table*
  [head & rows]
  (apply falx.scene/rows
         (coll->cols head)
         (map coll->cols rows)))

(defn htable*
  [head & rows]
  (apply falx.scene/cols
         (coll->rows head)
         (map coll->rows rows)))

(defn maptable
  ([kvs]
   (maptable kvs {}))
  ([kvs opts]
    ;;remember to support ordered (kv) colls too!
   (let [keys (map first kvs)
         vals (map second kvs)]
     (if (:vertical? opts)
       (falx.scene/rows
         (coll->cols keys)
         (coll->cols vals))
       (falx.scene/cols
         (coll->rows keys)
         (coll->rows vals))))))

(defn table
  [& kvs]
  (maptable (partition 2 kvs) {:vertical? true}))

(defn htable
  [& kvs]
  (maptable (partition 2 kvs) {:vertical? false}))