(ns falx.engine.ui
  "Contains functions on ui elements.

  Elements support 2 potential behaviours, drawing, and input handling.

  Valid ui elements include:

  - scenes of elements (either as an falx.scene/INode or via the `scene` fn)
  - any drawable thing implementing falx.draw.protocols/IDraw
  - any drawable thing implementing falx.draw.protocols/IDrawLater
  - strings"
  (:require [falx.engine.scene :as scene]
            [falx.engine.ui.protocols :as proto]
            [falx.engine.draw.protocols :as dproto]
            [falx.engine.draw :as d]
            [falx.engine.rect :as rect]
            [falx.engine.input :as input]
            [falx.engine.mouse :as mouse])
  (:import (clojure.lang Keyword Var)))

(defn drawfn
  "Returns a 1-ary fn that draws the element given a model."
  ([el rect]
   (proto/-drawfn el rect))
  ([el x y w h]
   (drawfn el [x y w h])))

(defn draw!
  "Draws the element given a model."
  ([el model input rect]
   (proto/-draw! el model input rect))
  ([el model input x y w h]
   (draw! el model input [x y w h])))

(defn handlefn
  "Returns a 1-ary fn that handles input for the element given a model and input.
  The handlefn returns a seq of events."
  ([el rect]
   (proto/-handlefn el rect))
  ([el x y w h]
   (handlefn el [x y w h])))

(defn handle
  "Handles input for the element given a model and input.
   Returns a seq of events."
  ([el model input rect]
   (proto/-handle el model input rect))
  ([el model input x y w h]
   (handle el model input [x y w h])))

(def ^:private noop
  (constantly nil))

(defmulti kw-handle (fn [k model input rect] k))
(defmethod kw-handle :default
  [_ model _]
  model)

(defmulti kw-handlefn (fn [k rect] k))
(defmethod kw-handlefn :default
  [k rect]
  #(kw-handle k %1 %2 rect))

(defmulti kw-draw! (fn [k model input rect] k))
(defmethod kw-draw! :default
  [k model input rect]
  (draw! (str k) model input rect))

(defmulti kw-drawfn (fn [k rect] k))
(defmethod kw-drawfn :default
  [k rect]
  #(kw-draw! k % rect))

(extend-protocol proto/IHandle
  nil
  (-handle [this model input rect]
    nil)
  Object
  (-handle [this model input rect]
    nil)
  Var
  (-handle [this model input rect]
    (handle (var-get this) model input rect))
  Keyword
  (-handle [this model input rect]
    (kw-handle this model input rect)))

(extend-protocol proto/IDraw
  nil
  (-draw! [this model input rect]
    (d/draw! this rect))
  Object
  (-draw! [this model input rect]
    (d/draw! this rect))
  Var
  (-draw! [this model input rect]
    (draw! (var-get this) model input rect))
  Keyword
  (-draw! [this model input rect]
    (kw-draw! this model input rect)))

(extend-protocol proto/IDrawLater
  nil
  (-drawfn [this rect]
    (fn [model input]
      (proto/-draw! this model input rect)))
  Object
  (-drawfn [this rect]
    (fn [model input]
      (proto/-draw! this model input rect)))
  Var
  (-drawfn [this rect]
    (drawfn (var-get this) rect))
  Keyword
  (-drawfn [this rect]
    (kw-drawfn this rect))
  falx.engine.draw.protocols.IDrawLater
  (-drawfn [this rect]
    (let [f (d/drawfn this rect)]
      (fn [_ _]
        (f)))))

(extend-protocol proto/IHandleLater
  nil
  (-handlefn [this rect]
    noop)
  Object
  (-handlefn [this rect]
    (fn [model input]
      (proto/-handle this model input rect)))
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
   (falx.engine.ui/scene scene nil))
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
                              (fn [model input]
                                (into [] (mapcat #(%1 model input)) fs))))
                          cache-handle? memoize)
         drawfn (cond-> (fn [rect]
                          (let [layout (layoutfn rect)
                                fs (mapv (fn [[el rect]]
                                           (proto/-drawfn el rect))
                                         layout)]
                            (fn [model input]
                              (run! #(% model input) fs))))
                        cache-draw? memoize)]
     (reify
       proto/IDraw
       (-draw! [this model input rect]
         ((drawfn rect) model input))
       proto/IDrawLater
       (-drawfn [this rect]
         (drawfn rect))
       proto/IHandle
       (-handle [this model input rect]
         ((handlefn rect) model input))
       proto/IHandleLater
       (-handlefn [this rect]
         (handlefn rect))))))

(defn- transient-scene
  ([scene]
   (falx.engine.ui/scene
     scene
     {:cache-layout? false
      :cache-handle? false
      :cache-draw? false})))

(extend-type falx.engine.scene.protocols.INode
  proto/IDraw
  (-draw! [this model input rect]
    (draw! (transient-scene this) model input rect))
  proto/IDrawLater
  (-drawfn [this rect]
    (drawfn (transient-scene this) rect))
  proto/IHandle
  (-handle [this model input rect]
    (handle (transient-scene this) model input rect))
  proto/IHandleLater
  (-handlefn [this rect]
    (handlefn (transient-scene this) rect)))

(defn env-call
  "Returns an element that evaluates the fn every time it is drawn.
  The fn should return an element."
  [f]
  (reify
    proto/IDraw
    (-draw! [this model input rect]
      (draw! (f) model input rect))
    dproto/ISized
    (-size [this w h]
      (dproto/-size (f) w h))))

(defmacro env
  "Returns an element that evaluates the body every time it is drawn.
  The body should return an element."
  [& body]
  `(env-call (fn [] ~@body)))

(defn dynamic
  "Returns an element dynamically by applying `f` to the model to obtain a ui element.
  e.g (draw (f model) model rect)"
  ([f]
   (reify
     proto/IDraw
     (-draw! [this model input rect]
       (proto/-draw! (f model) model input rect))
     proto/IHandle
     (-handle [this model input rect]
       (proto/-handle (f model) model input rect)))))

(defn target
  "Returns an element that will apply `f` to the model before drawing or handling"
  ([el f]
   (reify
     proto/IDraw
     (-draw! [this model input rect]
       (proto/-draw! el (f model) input rect))
     proto/IDrawLater
     (-drawfn [this rect]
       (let [dfn (drawfn el rect)]
         (fn [model input]
           (dfn (f model) input))))
     proto/IHandle
     (-handle [this model input rect]
       (proto/-handle el (f model) input rect))
     proto/IHandleLater
     (-handlefn [this rect]
       (let [hfn (handlefn el rect)]
         (fn [model input]
           (hfn (f model) input)))))))

(defrecord Switch [key m]
  proto/IDraw
  (-draw! [this model input rect]
    (let [el (get m (key model input rect))]
      (draw! el model input rect)))
  proto/IDrawLater
  (-drawfn [this rect]
    (let [m2 (into {} (map (juxt first #(drawfn (val %) rect))) m)]
      (fn [model input]
        (when-some [f (get m2 (key model input rect))]
          (f model input)))))
  proto/IHandle
  (-handle [this model input rect]
    (let [el (get m (key model input rect))]
      (handle el model input rect)))
  proto/IHandleLater
  (-handlefn [this rect]
    (let [m2 (into {} (map (juxt first #(handlefn (val %) rect))) m)]
      (fn [model input]
        (when-some [f (get m2 (key model input rect))]
          (f model input))))))

(defn switch
  "Forms a switch between several elements.
  `(key model input rect)` is evaluated on draw/handle
  The result of which is used to lookup an element in `m`."
  [key m]
  (->Switch key m))

(defn if-pred
  "Forms a conditional element.
  `(pred model input rect)` is evaluated, the result of which is used
  to pick either the `then` or `else` element."
  [pred then else]
  (switch
    (fn [model input rect]
      (if (pred model input rect)
        true
        false))
    {true then
     false else}))

(defn pred
  [f]
  (fn [model input _]
    (f model)))

(defrecord Behaviour [el fs]
  proto/IDraw
  (-draw! [this model input rect]
    (draw! el model input rect))
  proto/IDrawLater
  (-drawfn [this rect]
    (drawfn el rect))
  proto/IHandle
  (-handle [this model input rect]
    (concat
      (mapcat #(% model input rect) fs)
      (handle el model input rect)))
  proto/IHandleLater
  (-handlefn [this rect]
    (let [elf (handlefn el rect)]
      (fn [model input]
        (concat (mapcat #(% model input rect) fs)
                (elf model input))))))

(defn behaviour
  ([el & fs]
   (->Behaviour el (vec fs))))

(defn at-mouse
  [el]
  (reify proto/IDraw
    (-draw! [this model input rect]
      (let [rect (rect/put rect (-> input ::input/mouse ::mouse/point))]
        (draw! el model input rect)))
    proto/IHandle
    (-handle [this model input rect]
      (handle el model input rect))))