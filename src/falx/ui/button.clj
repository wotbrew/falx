(ns falx.ui.button
  (:require [falx.draw :as d]
            [falx.ui :as ui]
            [falx.mouse :as mouse]
            [falx.point :as pt]))

(defn- fnify
  [x]
  (if (fn? x) x (constantly x)))


(defn- statefn
  [view rect]
  (case (::state view)
    ::state.default ::d/button-state.default
    ::state.focused ::d/button-state.focused
    ::state.disabled ::d/button-state.disabled
    (if (mouse/in? view rect)
      ::d/button-state.focused
      ::d/button-state.default)))

(defn drawfn
  ([{:keys [text state]
     :or {text identity
          state statefn}}]
   (let [textfn (fnify text)
         statefn (fnify state)]
     (fn [view rect]
       (let [txt (textfn view)
             state (statefn view rect)]
         (d/drawfn
           (d/button txt state)
           rect))))))

(defn define
  [k & {:as kvs}]
  (ui/add-elem!
    k
    (merge
      {:drawfn (drawfn kvs)}
      kvs)))
