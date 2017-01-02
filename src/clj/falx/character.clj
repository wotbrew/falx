(ns falx.character
  (:require [falx.gdx :as gdx]
            [clojure.java.io :as io]
            [falx.ui :as ui]))

(def icon
  (gdx/texture-region ui/gui 96 0 32 32))

(def human
  (gdx/texture (io/resource "tiles/human.png")))

(def human-female
  (gdx/texture-region human 0 0 32 32))

(def human-male
  (gdx/texture-region human 32 0 32 32))

(def goblins
  (gdx/texture (io/resource "tiles/goblins.png")))

(def goblin-worker
  (gdx/texture-region goblins 0 0 32 32))

(def hairs
  {:human {:male
           (sorted-map
             0 nil
             1 (gdx/texture-region human 64 0 32 32))
           :female
           (sorted-map
             0 nil
             1 (gdx/texture-region human 0 32 32 32)
             2 (gdx/texture-region human 32 32 32 32)
             3 (gdx/texture-region human 64 32 32 32))}})

(def beards
  {:human {:male
           (sorted-map
             0 nil
             1 (gdx/texture-region human 96 0 32 32))}})

(def races
  (sorted-set :human :goblin))

(def genders
  {:human (sorted-set :male :female)
   :goblin (sorted-set :male :female)})

(defn draw-body!
  [{:keys [gender race hair beard]} x y w h]
  (let [htr (-> hairs (get race) (get gender) (get hair))
        btr (-> beards (get race) (get gender) (get beard))]
    (case (or race :human)
      :human
      (case gender
        :male (gdx/draw! human-male x y w h)
        (gdx/draw! human-female x y w h))
      :goblin
      (case gender
        :male (gdx/draw! goblin-worker x y w h)
        (gdx/draw! goblin-worker x y w h))

      nil)
    (when btr
      (gdx/draw! btr x y w h))
    (when htr
      (gdx/draw! htr x y w h))))

(defn body-drawable
  [m]
  (reify gdx/IDrawIn
    (draw-in! [this x y w h]
      (draw-body! m x y w h))))

(defn genbody
  []
  (let [race (rand-nth (vec (disj races :goblin)))
        gender (rand-nth (vec (genders race)))
        hair (rand-nth (some-> hairs race gender keys))
        beard (rand-nth (some-> beards race gender keys))]
    {:race   race
     :gender gender
     :hair   hair
     :beard  beard}))

(ui/defscene :stats
  ui/back-handler
  ui/breadcrumbs
  (ui/center
    (ui/resize
      320 280
      (ui/stack
        (ui/fancy-box 2)
        (ui/center "stats")))))

(defmethod ui/scene-name :stats [_] "Stats")