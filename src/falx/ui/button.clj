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

(defn handler
  [{:keys [click
           alt-click]}]
  (let [fs
        (->> [(when click
                (fn [gs rect]
                  (if (mouse/clicked-in? gs rect)
                    (click gs)
                    gs)))
              (when alt-click
                (fn [gs rect]
                  (if (mouse/alt-clicked-in? gs rect)
                    (alt-click gs)
                    gs)))]
             (filterv some?))]
    (fn [gs rect]
      (reduce #(%2 %1 rect) gs fs))))

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
      {:draw (drawfn kvs)
       :handler (handler kvs)}
      kvs)))
