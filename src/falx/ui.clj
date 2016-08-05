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
            [falx.mouse :as mouse]
            [falx.draw.protocols :as dproto]
            [falx.draw :as d])
  (:import (clojure.lang Keyword Var)))

(defn drawfn
  "Returns a 1-ary fn that draws the element given a gs."
  ([el rect]
   (proto/-drawfn el rect))
  ([el x y w h]
   (drawfn el [x y w h])))

(defn draw!
  "Draws the element given a gs."
  ([el gs rect]
   (proto/-draw! el gs rect))
  ([el gs x y w h]
   (draw! el gs [x y w h])))

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

(defmulti kw-handle (fn [k gs rect] k))
(defmethod kw-handle :default
  [_ gs _]
  gs)

(defmulti kw-handlefn (fn [k rect] k))
(defmethod kw-handlefn :default
  [k rect]
  #(kw-handle k % rect))

(defmulti kw-draw! (fn [k gs rect] k))
(defmethod kw-draw! :default
  [k gs rect]
  (draw! (str k) gs rect))

(defmulti kw-drawfn (fn [k rect] k))
(defmethod kw-drawfn :default
  [k rect]
  #(kw-draw! k % rect))

(extend-protocol proto/IHandle
  nil
  (-handle [this gs rect]
    gs)
  Object
  (-handle [this gs rect]
    gs)
  Var
  (-handle [this gs rect]
    (handle (var-get this) gs rect))
  Keyword
  (-handle [this gs rect]
    (kw-handle this gs rect)))

(extend-protocol proto/IDraw
  nil
  (-draw! [this gs rect]
    (d/draw! this rect))
  Object
  (-draw! [this gs rect]
    (d/draw! this rect))
  Var
  (-draw! [this gs rect]
    (draw! (var-get this) gs rect))
  Keyword
  (-draw! [this gs rect]
    (kw-draw! this gs rect)))

(extend-protocol proto/IDrawLater
  nil
  (-drawfn [this rect]
    (fn [gs]
      (proto/-draw! this gs rect)))
  Object
  (-drawfn [this rect]
    (fn [gs]
      (proto/-draw! this gs rect)))
  Var
  (-drawfn [this rect]
    (drawfn (var-get this) rect))
  Keyword
  (-drawfn [this rect]
    (kw-drawfn this rect))
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
      (proto/-handle this gs rect)))
  Var
  (-handlefn [this rect]
    (handlefn (var-get this) rect))
  Keyword
  (-handlefn [this rect]
    (kw-handlefn this rect)))

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
                            (fn [gs]
                              (run! #(% gs) fs))))
                        cache-draw? memoize)]
     (reify
       proto/IDraw
       (-draw! [this gs rect]
         ((drawfn rect) gs))
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

(extend-type falx.scene.protocols.INode
  proto/IDraw
  (-draw! [this gs rect]
    (draw! (transient-scene this) gs rect))
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
    (-draw! [this gs rect]
      (draw! (f) gs rect))
    dproto/ISized
    (-size [this w h]
      (dproto/-size (f) w h))))

(defmacro env
  "Returns an element that evaluates the body every time it is drawn.
  The body should return an element."
  [& body]
  `(env-call (fn [] ~@body)))

(defn bind
  "Returns an element that applies `f` to the gs before operating on the result.
  e.g (draw (f gs) gs rect)"
  ([f]
   (reify
     proto/IDraw
     (-draw! [this view rect]
       (proto/-draw! (f view) view rect))
     proto/IHandle
     (-handle [this gs rect]
       (proto/-handle (f gs) gs rect)))))

(defrecord Switch [key m]
  proto/IDraw
  (-draw! [this gs rect]
    (let [el (get m (key gs rect))]
      (draw! el gs rect)))
  proto/IDrawLater
  (-drawfn [this rect]
    (let [m2 (into {} (map (juxt first #(drawfn (val %) rect))) m)]
      (fn [gs]
        (when-some [f (get m2 (key gs rect))]
          (f gs)))))
  proto/IHandle
  (-handle [this gs rect]
    (let [el (get m (key gs rect))]
      (handle el gs rect)))
  proto/IHandleLater
  (-handlefn [this rect]
    (let [m2 (into {} (map (juxt first #(handlefn (val %) rect))) m)]
      (fn [gs]
        (if-some [f (get m2 (key gs rect))]
          (f gs)
          gs)))))

(defn switch
  "Forms a switch between several elements.
  `(key gs rect)` is evaluated on draw/handle
  The result of which is used to lookup an element in `m`."
  [key m]
  (->Switch key m))

(defn mouse-in?
  "Is the mouse in the given rect?"
  [gs rect]
  (mouse/in? (::mouse/mouse gs) rect))

(defn mouse-click?
  ([gs]
   (mouse/left-click? (::mouse/mouse gs)))
  ([gs rect]
    (mouse/left-click? (::mouse/mouse gs) rect)))

(defn mouse-alt-click?
  ([gs]
   (mouse/right-click? (::mouse/mouse gs)))
  ([gs rect]
   (mouse/right-click? (::mouse/mouse gs) rect)))

(defn if-pred
  "Forms a conditional element.
  `(pred gs rect)` is evaluated, the result of which is used
  to pick either the `then` or `else` element."
  [pred then else]
  (switch
    (fn [gs rect]
      (if (pred gs rect)
        true
        false))
    {true then
     false else}))

(defn if-focus
  "If the rect has focus, `then` is picked, otherwise `else` is picked."
  [then else]
  (if-pred mouse-in?
           then
           else))

(defrecord Behaviour [el fs]
  proto/IDraw
  (-draw! [this gs rect]
    (draw! el gs rect))
  proto/IDrawLater
  (-drawfn [this rect]
    (drawfn el rect))
  proto/IHandle
  (-handle [this gs rect]
    (as-> gs gs
          (reduce #(%2 %1 rect) gs fs)
          (handle el gs rect)))
  proto/IHandleLater
  (-handlefn [this rect]
    (let [elf (handlefn el rect)]
      (fn [gs]
        (-> (reduce #(%2 %1 rect) gs fs)
            (elf))))))

(defn behaviour
  ([el & fs]
    (->Behaviour el (vec fs))))

(defn if-click
  [then else]
  (if-pred mouse-click? then else))

(defn if-alt-click
  [then else]
  (if-pred mouse-alt-click? then else))

(defn click
  [el f]
  (if-click (behaviour el f) el))

(defn alt-click
  [el f]
  (if-alt-click (behaviour el f) el))

(defn button
  "Returns a button element.
  opts
   - `:disable-pred` a predicate function of gs, rect that can return whether or not the button should be disabled"
  ([s]
   (button s nil))
  ([s opts]
   (if-pred (:disable-pred opts (constantly false))
     (d/button s {:disabled? true})
     (if-focus
       (cond-> (d/button s {:focused? true})
               (:click opts) (click (:click opts))
               (:alt-click opts) (alt-click (:alt-click opts)))
       (d/button s)))))

(defn at-mouse
  ([el size]
   (let [[w h] size]
     (at-mouse el w h)))
  ([el w h]
    (reify proto/IDraw
      (-draw! [this gs rect]
        (let [mouse (::mouse/mouse gs)
              [x y] (::mouse/point mouse [0 0])]
          (draw! el gs [x y w h]))))))