(ns falx.screen
  "Functions on screens"
  (:require [falx.config :as config]
            [falx.engine.ui :as ui]
            [falx.engine.input :as input]
            [falx.engine.mouse :as mouse]
            [falx.engine.draw :as d]
            [falx.engine.scene :as scene]
            [falx.sprite :as sprite])
  (:import (java.util UUID)))

(defmulti scene (fn [key] key))

(defmethod scene :default
  [key]
  (str "Unknown scene: " key))

(defmacro defscene
  [key scene]
  `(let [key# ~key]
     (if config/optimise?
       (let [scene# (ui/scene ~scene)]
         (defmethod scene key# [_#] scene#))
       (defmethod scene key# [_#] ~scene))))

(defn screen
  [key size]
  {::scene key
   ::size size
   ::state {}
   ::input {}})

(defn handle
  ([screen input]
   (let [[w h :as size] (::size screen)
         key (::scene screen)
         scene (scene key)
         screen (assoc screen ::input {})]
     (ui/handle scene screen input [0 0 w h]))))

(defn draw!
  ([screen input]
   (let [[w h] (::size screen)
         key (::scene screen)
         scene (scene key)]
     (ui/draw! scene screen input [0 0 w h]))))

(defn goto
  [screen key]
  (assoc screen ::scene key))

(defn- dissoc-in
  [m [k & ks]]
  (if (seq ks)
    (if-some [m2 (not-empty (dissoc-in (get m k) ks))]
      (assoc m k m2)
      (dissoc m k))
    (dissoc m k)))

(defn expand
  [screen id]
  (-> (assoc-in screen [::state id ::expanded?] true)
      (update ::expanded (fnil conj #{}) id)))

(defn unexpand
  [screen id]
  (-> (dissoc-in screen [::state id ::expanded?])
      (update ::expanded disj id)))

(defn expanded?
  [screen id]
  (-> screen ::state (get id) ::expanded?))

(defn toggle-expand
  [screen id]
  (if (expanded? screen id)
    (unexpand screen id)
    (expand screen id)))

(defn ensure-id
  [opts]
  (if (some? (:id opts))
    opts
    (assoc opts :id (UUID/randomUUID))))

(defn if-selected
  [id then else]
  (ui/if-pred
    (ui/pred #(-> % ::state (get id) ::selected?))
    then
    else))

(defn if-disabled
  [id then else]
  (ui/if-pred
    (ui/pred #(-> % ::state (get id) ::disabled?))
    then
    else))

(defn focused-pred
  [id]
  (fn [screen input rect]
    (and
      (not (-> screen ::state (get id) ::disabled?))
      (let [mouse (::input/mouse input)]
        (mouse/in? mouse rect)))))

(defn if-focused
  [id then else]
  (ui/if-pred
    (focused-pred id)
    then
    else))

(defn expanded-pred
  [id]
  (ui/pred #(expanded? % id)))

(defn if-expanded
  [id then else]
  (ui/if-pred
    (expanded-pred id)
    then
    else))

(def black
  [0 0 0 1])

(def background
  (d/recolor d/pixel black))

(def dark-grey
  [0.3 0.3 0.3 1])

(def yellow
  [0.7 0.7 0 1])

(def green
  [0 1 0 1])

(def white
  [1 1 1 1])

(def off-white
  [0.7 0.7 0.7 1])

(defn text-disabled
  [s]
  (d/text s {:centered? true :color dark-grey}))

(defn text-selected
  [s]
  (d/text s {:centered? true :color green}))

(defn text-focused
  ([s]
   (text-focused s {}))
  ([s opts]
   (let [s (if (:text-dash? opts)
             (str "- " s " -")
             s)]
     (d/text s {:centered? true :color white}))))

(defn text-default
  [s]
  (d/text s {:centered? true :color off-white}))

(defn- button-box
  [color]
  (d/box {:color color
          :shaded? true
          :thickness 2}))

(defn- button*
  [text color]
  (scene/stack
    background
    (button-box color)
    text))

(defn- button-disabled
  [s]
  (button* (text-disabled s) dark-grey))

(defn- button-selected
  [s]
  (button* (text-selected s) green))

(defn- focused-input-behaviour
  [id]
  (fn [screen input rect]
    (let [hit (input/hit-set input)
          pressed (input/pressed-set input)]
      (if (or (seq hit) (seq pressed))
        (assoc-in screen [::input id]
                  {::hit (input/hit-set input)
                   ::pressed (input/pressed-set input)})
        screen))))

(defn- button-focused
  ([s]
   (button-focused s nil))
  ([s opts]
   (cond-> (button* (text-focused s opts) white)
           (:id opts) (ui/behaviour (focused-input-behaviour (:id opts)))
           (:focused-behavior opts) (ui/behaviour (:focused-behavior opts)))))

(defn- button-default
  [s]
  (button* (text-default s) off-white))

(defn button
  ([s]
   (button s {}))
  ([s opts]
   (let [opts (ensure-id opts)
         id (:id opts)]
     (->> (button-default s)
          (if-selected id (button-selected s))
          (if-focused id (button-focused s opts))
          (if-disabled id (button-disabled s))))))

(defn nav-button
  ([s]
   (button s {}))
  ([s opts]
   (let [opts (merge
                opts
                {:focused-behavior
                 (fn [screen input rect]
                   (if (input/clicked? input)
                     (goto screen (or (:goto opts) (::scene screen)))
                     screen))})]
     (button s opts))))

(defn icon
  [img]
  (scene/fit img [32 32]))

(defn hover-img
  [img opts]
  (let [opts (ensure-id opts)
        id (:id opts)
        recol (d/recolor img green)
        behav (cond->
                recol
                (:focused-behavior opts)
                (ui/behaviour
                  (:focused-behavior opts)))]
    (if-focused id behav img)))

(defn hover-icon
  [img opts]
  (-> (hover-img img opts)
      icon))

(def mouse
  (ui/at-mouse (scene/fit sprite/mouse-point config/cell-size)))