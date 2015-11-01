(ns gdx.camera
  (:require [gdx.display :as display]
            [gdx.dispatch :as dispatch]
            [gdx.batch :as batch])
  (:import (com.badlogic.gdx.graphics OrthographicCamera Camera)
           (com.badlogic.gdx.math Vector3)))

(defonce ortho-instance (delay (OrthographicCamera.)))

(defn set-to-ortho!
  [^OrthographicCamera gdx-camera width height flip-y?]
  (dispatch/on-render-thread
    (.setToOrtho gdx-camera (boolean flip-y?) (float width) (float height))))

(defn get-combined
  [^Camera gdx-camera]
  (.-combined gdx-camera))

(defn set-position!
  [gdx-camera x y]
  (let [^Vector3 pos (.-position gdx-camera)]
    (set! (.-x pos) (float x))
    (set! (.-y pos) (float y))))

(defn update!
  [^Camera gdx-camera]
  (.update gdx-camera))

(defn ortho-camera
  [& {:keys [flip-y? size location] :as opts}]
  (let [size (or size (:size display/default))
        [w h] size]
    (merge
      {:type :camera/orthographic
       :pos  [(float (/ w 2))
              (float (/ h 2))]
       :size size}
      opts)))

(defmulti sync-camera! :type)

(defmethod sync-camera! :camera/orthographic
  [camera]
  (let [{:keys [pos size flip-y?]} camera
        [x y] pos
        [w h] size]
    (dispatch/on-render-thread
      (doto @ortho-instance
        (set-to-ortho! w h flip-y?)
        (set-position! x y)
        update!))
    nil))

(defmulti get-gdx-camera :type)

(defmethod get-gdx-camera :camera/orthographic
  [camera]
  @ortho-instance)

(defmacro using-gdx-camera
  [batch gdx-camera & body]
  `(batch/using-projection-matrix
     ~batch
     (get-combined ~gdx-camera)
     ~@body))

(defmacro using-camera
  [batch camera & body]
  `(let [c# ~camera]
     (using-gdx-camera
       ~batch (do (sync-camera! c#)
                  (get-gdx-camera c#))
       ~@body)))

(def camera ortho-camera)