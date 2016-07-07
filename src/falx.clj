(ns falx
  (:require [clj-gdx :as gdx]
            [clojure.tools.logging :refer [error info debug]]
            [clojure.core.async :as async]
            [falx.scene :as scene]
            [falx.geom :as g]
            [falx.sprite :as sprite]))

(def max-fps
  60)

(def app
  (merge
    gdx/default-app
    {:max-background-fps max-fps
     :max-foreground-fps max-fps}))

(def game
  (atom {}))

(def in
  (async/chan))

(def scene-order
  (->> [::scene/type
        :fps
        :mouse]
       (map-indexed (fn [i x] [x i]))
       (into {})))

(def scene-comparator
  #(compare (scene-order %1 0)
            (scene-order %2 0)))

(def scene
  (atom
    (scene/tree
      (sorted-map-by scene-comparator
                     :fps (scene/text
                            {::scene/geom (g/rect 32 32 64 64)
                             ::scene/text 0})
                     :mouse (scene/sprite
                              {::scene/geom (g/point)
                               ::scene/sprite sprite/mouse-point})))))

(defn render!
  [game]
  (swap! scene assoc-in [:fps ::scene/text] (gdx/get-fps))
  (swap! scene assoc-in [:mouse ::scene/geom] (g/point-tuple->geom (:point @gdx/mouse-state)))
  (scene/draw!
    @scene))

(gdx/defrender
  (let [g @game]
    (try
      (render! g)
      (catch Throwable e
        (error e)
        (Thread/sleep 5000)))))

(defn -main
  [& args]
  (gdx/start-app! app))