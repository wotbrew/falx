(ns falx.ui
  (:require [falx.draw :as d]
            [falx.rect :as rect]
            [falx.scene :as scene]))

(defn state
  [gs k]
  (-> gs ::state (get k)))

(defn reset-state
  [gs k state]
  (assoc-in gs [::state k] state))

(defn alter-state
  ([gs k f]
   (let [st (state gs k)]
     (reset-state gs k (f st))))
  ([gs k f & args]
   (alter-state gs k #(apply f % args))))

(defonce ^:private elem-registry
  (atom {}))

(defrecord Elem [view
                 drawfn
                 handler])

(def noop
  (constantly nil))

(def defaults
  {:view identity
   :draw (constantly noop)
   :handler (fn [gs rect] gs)})

(defn elem
  [m]
  (map->Elem (merge defaults m)))

(defn add-elem!
  [k m]
  (swap! elem-registry assoc k (elem m))
  nil)

(defn defelem
  [k & {:as kvs}]
  (add-elem! k kvs))

(defonce ^:private scene-registry
  (atom {}))

(defn add-scene!
  [k scene]
  (swap! scene-registry assoc k scene)
  nil)

(defn defscene
  [k scene]
  (add-scene! k scene))

(defelem ::missing-scene
  :draw (fn [gs rect]
          (d/drawfn
            (d/text
              (str "Scene missing!: " (pr-str (::scene gs)))
              d/text-font
              {:centered? true})
            rect)))

(defn get-scene
  [k]
  (get @scene-registry k ::missing-scene))

(defn scene-rect
  [gs]
  [0 0 800 600])

(defn draw!
  ([gs]
   (let [scene (get-scene (::scene gs))
         rect (scene-rect gs)]
     (draw! (scene/layout scene rect) gs)))
  ([layout gs]
   (let [elem @elem-registry
         scene @scene-registry]
     (->
       (fn [[k rect]]
         (if-some [elem (elem k)]
           (let [vfn (:view elem)
                 view (vfn gs)
                 ddraw! ((:draw elem) view rect)]
             (when ddraw! (ddraw!)))
           (when-some [scene (scene k)]
             (draw! (scene/layout scene rect) gs))))
       (run! layout)))))

(defn handle
  ([gs]
    (let [scene (get-scene (::scene gs))
          rect (scene-rect gs)]
      (handle gs (scene/layout scene rect))))
  ([gs layout]
   (let [elem @elem-registry]
     (->
       (fn [gs [k rect]]
         (if-some [elem (elem k)]
           ((:handler elem) gs rect)
           gs))
       (reduce gs layout)))))