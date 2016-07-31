(ns falx.ui
  "Contains functions on ui elements.

  Elements support 2 potential behaviours, drawing, and input handling.

  Valid ui elements include:

  - scenes of elements (either as an falx.scene/INode or via the `scene` fn)
  - any drawable thing implementing falx.draw.protocols/IDraw
  - any drawable thing implementing falx.draw.protocols/IDrawLater
  - strings"
  (:require [falx.scene :as scene]
            [falx.ui.protocols :as proto]
            [falx.draw.protocols :as dproto]
            [falx.draw :as d]))

(defn drawfn
  "Returns a 1-ary fn that draws the element given a view."
  ([el rect]
   (proto/-drawfn el rect))
  ([el x y w h]
   (drawfn el [x y w h])))

(defn draw!
  "Draws the element given a view."
  ([el view rect]
   (proto/-draw! el view rect))
  ([el view x y w h]
   (draw! el view [x y w h])))

(defn handlefn
  "Returns a 1-ary fn that handles input for the element given a game state.
  The handlefn returns a new game state."
  ([el rect]
   (proto/-handlefn el rect))
  ([el x y w h]
   (handlefn el [x y w h])))

(defn handle
  "Handles input for the element given a game state.
  Returns a new game state."
  ([el gs rect]
   (proto/-handle el gs rect))
  ([el gs x y w h]
   (handle el gs [x y w h])))

(def ^:private noop
  (constantly nil))

(extend-protocol proto/IHandle
  nil
  (-handle [this gs rect]
    gs)
  Object
  (-handle [this gs rect]
    gs))

(extend-protocol proto/IDraw
  nil
  (-draw! [this view rect]
    (d/draw! this rect))
  Object
  (-draw! [this view rect]
    (d/draw! this rect)))

(extend-protocol proto/IDrawLater
  nil
  (-drawfn [this rect]
    (fn [view]
      (proto/-draw! this view rect)))
  Object
  (-drawfn [this rect]
    (fn [view]
      (proto/-draw! this view rect)))
  falx.draw.protocols.IDrawLater
  (-drawfn [this rect]
    (let [f (d/drawfn this rect)]
      (fn [_]
        (f)))))

(extend-protocol proto/IHandleLater
  nil
  (-handlefn [this rect]
    identity)
  Object
  (-handlefn [this rect]
    (fn [gs]
      (proto/-handle this gs rect))))

(defn scene
  "Returns an element for an entire scene.
  Using this function will cache what it can, making it potentially faster
  than using the scene as an element directly.
  opts
   `:cache-layout?` whether or not to cache the layout results (true).
   `:cache-handle?` whether or not to cache the handle fn (true).
   `:cache-draw?` whether or not to cache the draw fn (true)."
  ([scene]
   (falx.ui/scene scene nil))
  ([scene opts]
   (let [{:keys [cache-layout?
                 cache-handle?
                 cache-draw?]
          :or {cache-layout? true
               cache-handle? true
               cache-draw? true}} opts
         layoutfn (cond-> (partial scene/layout scene)
                          cache-layout? memoize)
         handlefn (cond-> (fn [rect]
                            (let [layout (layoutfn rect)
                                  fs (mapv (fn [[el rect]]
                                             (proto/-handlefn el rect))
                                           layout)]
                              (fn [gs]
                                (reduce #(%2 %1) gs fs))))
                          cache-handle? memoize)
         drawfn (cond-> (fn [rect]
                          (let [layout (layoutfn rect)
                                fs (mapv (fn [[el rect]]
                                           (proto/-drawfn el rect))
                                         layout)]
                            (fn [view]
                              (run! #(% view) fs))))
                        cache-draw? memoize)]
     (reify
       proto/IDraw
       (-draw! [this view rect]
         ((drawfn rect) view))
       proto/IDrawLater
       (-drawfn [this rect]
         (drawfn rect))
       proto/IHandle
       (-handle [this gs rect]
         ((handlefn rect) gs))
       proto/IHandleLater
       (-handlefn [this rect]
         (handlefn rect))))))

(defn- transient-scene
  ([scene]
   (falx.ui/scene
     scene
     {:cache-layout? false
      :cache-handle? false
      :cache-draw? false})))

(extend-type falx.scene.INode
  proto/IDraw
  (-draw! [this view rect]
    (draw! (transient-scene this) view rect))
  proto/IDrawLater
  (-drawfn [this rect]
    (drawfn (transient-scene this) rect))
  proto/IHandle
  (-handle [this gs rect]
    (handle (transient-scene this) gs rect))
  proto/IHandleLater
  (-draw! [this rect]
    (handlefn (transient-scene this) rect)))

(defn env-call
  "Returns an element that evaluates the fn every time it is drawn.
  The fn should return an element."
  [f]
  (reify
    proto/IDraw
    (-draw! [this view rect]
      (draw! (f) view rect))
    dproto/ISized
    (-size [this w h]
      (dproto/-size (f) w h))))

(defmacro env
  "Returns an element that evaluates the body every time it is drawn.
  The body should return an element."
  [& body]
  `(env-call (fn [] ~@body)))

