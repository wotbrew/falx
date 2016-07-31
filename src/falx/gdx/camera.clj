(ns falx.gdx.camera
  (:require [falx.gdx.impl.dispatch :as dispatch]
            [falx.gdx.batch :as batch])
  (:import (com.badlogic.gdx.graphics OrthographicCamera Camera)
           (com.badlogic.gdx.math Vector3)))

(defn camera
  [size]
  (let [[w h] size]
    (dispatch/dispatch
      (doto (OrthographicCamera.)
        (.setToOrtho true (float w) (float h))))))

(defn set-size!
  ([cam size]
   (let [[w h] size]
     (set-size! cam w h)))
  ([cam w h]
   (.setToOrtho ^OrthographicCamera cam true (float w) (float h))))

(defn set-pos!
  ([cam pt]
   (let [[x y] pt]
     (set-pos! cam x y)))
  ([cam x y]
   (let [^Vector3 pos (.-position cam)]
     (.set pos (float x) (float y) 0))))

(defmacro with
  [batch cam & body]
  `(let [^Camera cam# ~cam]
     (.update cam#)
     (batch/with-projection
       ~batch
       (.-combined cam#)
       ~@body)))

(defn screen-pt
  ([cam pt]
   (let [[x y] pt]
     (screen-pt cam x y)))
  ([cam x y]
   (dispatch/dispatch
     (let [v3 (Vector3. x y 1)]
       (.project ^Camera cam v3)
       [(int (.-x v3))
        (int (.-y v3))]))))

(defn world-pt
  ([cam pt]
   (let [[x y] pt]
     (world-pt cam x y)))
  ([cam x y]
   (dispatch/dispatch
     (let [v3 (Vector3. x y 1)]
       (.unproject ^Camera cam v3)
       [(int (.-x v3))
        (int (.-y v3))]))))