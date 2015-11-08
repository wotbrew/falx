(ns gdx.texture-region
  (:require [gdx.texture :as texture]
            [gdx.batch :as batch])
  (:refer-clojure :exclude [find])
  (:import (com.badlogic.gdx.graphics.g2d TextureRegion)
           (com.badlogic.gdx.graphics Texture)
           (java.util HashMap Map)))

(defn texture-region
  [texture rect & {:keys [flip-x?
                          flip-y?]
                   :as opts}]
  (merge
    {:type    :resource/texture-region
     :texture texture
     :rect    rect}
    opts))

(def cache (HashMap.))

(defn load!
  [region]
  (let [^Texture texture (texture/find (:texture region))
        [x y w h] (:rect region)
        gdx-region (doto (TextureRegion. texture (int x) (int y) (int w) (int h))
                     (.flip (boolean (:flip-x? region)) (boolean (:flip-y? region))))]
    (.put cache region gdx-region)
    (.get cache region)))

(defn unload!
  [region]
  (.remove cache region)
  nil)

(defn find
  [region]
  (or (.get ^Map cache region)
      (load! region)))