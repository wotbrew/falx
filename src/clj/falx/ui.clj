(ns falx.ui
  (:require [falx.gdx :as gdx]
            [clojure.java.io :as io]
            [falx.ui.protocols :refer :all]
            [falx.state :as state]
            [clojure.string :as str])
  (:import (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx Input$Buttons Input$Keys)))

(extend-protocol IScreenObject
  Object
  (-handle! [this frame x y w h]
    (gdx/draw-in! this x y w h)))

(extend-protocol IMeasure
  Object
  (measure [this frame x y w h]
    [w h])
  String
  (measure [this frame x y w h]
    (gdx/measure this w h)))

(defn contains-loc?
  ([x y w h loc]
   (let [[x2 y2] loc]
     (contains-loc? x y w h x2 y2)))
  ([x y w h x2 y2]
   (and (<= x x2 (+ x w -1))
        (<= y y2 (+ y h -1)))))

(defn handle!
  ([obj frame]
   (let [[w h] (-> frame :tick :config :size)]
     (handle! obj frame 0 0 w h)))
  ([obj frame x y w h]
   (-handle! obj frame x y w h)))

(defn recolor
  [color el]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (gdx/with-color color
        (-handle! el frame x y w h)))
    IMeasure
    (measure [this frame x y w h]
      (measure el frame x y w h))))

(defn tint
  [color el]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (gdx/with-tint color
        (-handle! el frame x y w h)))
    IMeasure
    (measure [this frame x y w h]
      (measure el frame x y w h))))

(defn switch-elem
  [f m]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (let [k (f frame x y w h)]
        (when-some [o (get m k)]
          (-handle! o frame x y w h))))
    IMeasure
    (measure [this frame x y w h]
      (let [k (f frame x y w h)]
        (when-some [o (get m k)]
          (measure o frame x y w h))))))


(def nil-elem
  (reify IScreenObject
    (-handle! [this frame x y w h])
    IMeasure
    (measure [this frame x y w h]
      [0 0])))

(defn if-elem
  ([pred then]
   (if-elem pred then nil-elem))
  ([pred then else]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (if (pred frame x y w h)
         (-handle! then frame x y w h)
         (-handle! else frame x y w h)))
     IMeasure
     (measure [this frame x y w h]
       (if (pred frame x y w h)
         (measure then frame x y w h)
         (measure else frame x y w h))))))

(defn gs-pred
  ([f]
   (fn [frame _ _ _ _]
     (f (:game frame))))
  ([f & args]
   (gs-pred #(apply f % args))))

(defn mouse-in?
  [frame x y w h]
  (contains-loc? x y w h (-> frame :tick :mouse-loc)))

(defn clicked?
  [frame x y w h]
  (and (mouse-in? frame x y w h)
       (let [click-button (-> frame :game-state :settings :click-button (or Input$Buttons/LEFT))]
         (-> frame :tick :buttons-hit (contains? click-button)))))

(defn alt-clicked?
  [frame x y w h]
  (and (mouse-in? frame x y w h)
       (let [click-button (-> frame :game-state :settings :alt-click-button (or Input$Buttons/LEFT))]
         (-> frame :tick :buttons-hit (contains? click-button)))))

(defn if-hovering
  ([then]
   (if-hovering then nil-elem))
  ([then else]
   (if-elem mouse-in?
     then else)))

(defn resize
  ([loc el]
   (let [[w h] loc]
     (resize w h el)))
  ([w h el]
   (reify IScreenObject
     (-handle! [this frame x y _ _]
       (-handle! el frame x y w h))
     IMeasure
     (measure [this frame _ _ _ _]
       [w h]))))

(defn restrict-width
  ([w el]
   (reify IScreenObject
     (-handle! [this frame x y _ h]
       (-handle! el frame x y w h))
     IMeasure
     (measure [this frame _ _ _ h]
       [w h]))))

(defn restrict-height
  ([h el]
   (reify IScreenObject
     (-handle! [this frame x y w _]
       (-handle! el frame x y w h))
     IMeasure
     (measure [this frame _ _ w _]
       [w h]))))

(defn stack
  ([] nil-elem)
  ([a] a)
  ([a b]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h))
     IMeasure
     (measure [this frame x y w h]
       (let [[x2 y2] (measure a frame x y w h)
             [x3 y3] (measure b frame x y w h)]
         [(max x2 x3) (max y2 y3)]))))
  ([a b c]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h)
       (-handle! c frame x y w h))
     IMeasure
     (measure [this frame x y w h]
       (let [[x2 y2] (measure a frame x y w h)
             [x3 y3] (measure b frame x y w h)
             [x4 y4] (measure c frame x y w h)]
         [(max x2 x3 x4) (max y2 y3 y4)]))))
  ([a b c d]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h)
       (-handle! c frame x y w h)
       (-handle! d frame x y w h))
     IMeasure
     (measure [this frame x y w h]
       (let [[x2 y2] (measure a frame x y w h)
             [x3 y3] (measure b frame x y w h)
             [x4 y4] (measure c frame x y w h)
             [x5 y5] (measure d frame x y w h)]
         [(max x2 x3 x4 x5) (max y2 y3 y4 y5)]))))
  ([a b c d & more]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! a frame x y w h)
       (-handle! b frame x y w h)
       (-handle! c frame x y w h)
       (-handle! d frame x y w h)
       (run! #(-handle! % frame x y w h) more))
     IMeasure
     (measure [this frame x y w h]
       (let [[x2 y2] (measure a frame x y w h)
             [x3 y3] (measure b frame x y w h)
             [x4 y4] (measure c frame x y w h)
             [x5 y5] (measure d frame x y w h)
             extra (map #(measure % frame x y w h) more)]
         [(apply max x2 x3 x4 x5 (map first extra))
          (apply max y2 y3 y4 y5 (map second extra))])))))


(defn center-rect
  ([size rect]
   (let [[x2 y2 w2 h2] rect]
     (center-rect size x2 y2 w2 h2)))
  ([size x2 y2 w2 h2]
   (center-rect (nth size 0) (nth size 1) x2 y2 w2 h2))
  ([w1 h1 x2 y2 w2 h2]
   (let [nw (min w1 w2)
         nh (min h1 h2)
         wdiff (- w2 nw)
         hdiff (- h2 nh)]
     [(long (+ x2 (/ wdiff 2)))
      (long (+ y2 (/ hdiff 2)))
      nw
      nh])))

(defn draw-fancy-box!
  ([x y w h]
   (draw-fancy-box! x y w h 1))
  ([x y w h t]
   (let [x (float x)
         y (float y)
         w (float w)
         h (float h)
         t (float t)]
     (gdx/draw-pixel! x y w t)
     (gdx/draw-pixel! x y t h)
     (gdx/with-tint Color/DARK_GRAY
       (gdx/draw-pixel! x (+ y h (- t)) w t)
       (gdx/draw-pixel! (+ x w (- t)) y t h)))))

(defn fancy-box
  [n]
  (reify gdx/IDrawIn
    (draw-in! [this x y w h]
      (draw-fancy-box! x y w h n))))

(defn center
  ([el]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (let [[w2 h2] (measure el frame x y w h)
             [x y w h] (center-rect w2 h2 x y w h)]
         (-handle! el frame x y w h)))
     IMeasure
     (measure [this frame x y w h]
       (let [[w2 h2] (measure el frame x y w h)
             [w h] (center-rect w2 h2 x y w h)]
           [w h])))))

(defn pad
  ([x2 y2 el]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! el frame (+ x x2) (+ y y2) (- w x2 x2) (- h y2 y2)))
     IMeasure
     (measure [this frame x y w h]
       [(- w x2 x2) (- h y2 y2)])))
  ([x2 y2 el & els]
   (pad x2 y2 (apply stack el els))))

(defn translate
  ([x2 y2 el]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! el frame (+ x x2) (+ y y2) w h))))
  ([x2 y2 el & els]
   (translate x2 y2 (apply stack el els))))

(defn add-size
  ([w2 h2 el]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! el frame x y (+ w w2) (+ h h2)))
     IMeasure
     (measure [this frame x y w h]
       (measure el frame x y (+ w w2) (+ h h2))))))

(def black
  (gdx/blank Color/BLACK))

(defn rows
  [& els]
  (let [els (vec els)]
    (if (empty? els)
      nil-elem
      (reify IScreenObject
        (-handle! [this frame x y w h]
          (let [row-height (long (/ h (count els)))]
            (loop [row 0]
              (when (< row (count els))
                (handle! (els row) frame x (+ y (* row row-height)) w row-height)
                (recur (inc row))))))))))

(defn fixed-rows
  [row-height & els]
  (let [els (vec els)]
    (reify IScreenObject
      (-handle! [this frame x y w _]
        (loop [row 0]
          (when (< row (count els))
            (handle! (els row) frame x (+ y (* row row-height)) w row-height)
            (recur (inc row))))))))

(defn cols
  [& els]
  (let [els (vec els)]
    (if (empty? els)
      nil-elem
      (reify IScreenObject
        (-handle! [this frame x y w h]
          (let [col-width (long (/ w (count els)))]
            (loop [col 0]
              (when (< col (count els))
                (handle! (els col) frame  (+ x (* col col-width)) y col-width h)
                (recur (inc col))))))))))

(defn flow
  [& els]
  (let [els (vec els)]
    (if (empty? els)
      nil-elem
      (reify IScreenObject
        (-handle! [this frame x y w h]
          (let [col-width (long (/ w (count els)))]
            (loop [i 0
                   mh 0
                   xoff 0
                   yoff 0]
              (when (< i (count els))
                (let [[w2 h2] (measure (els i) frame
                                       (+ x xoff)
                                       (+ y yoff)
                                       w h)
                      xoff2 (+ xoff w2)
                      mh2 (max mh h2)
                      yoff2 (if (< xoff2 w) yoff (+ yoff mh2))]
                  (handle! (els i) frame (+ x xoff) (+ y yoff) w2 h2)
                  (recur (inc i)
                         (if (= yoff2 yoff)
                           mh2
                           0)
                         (if (= yoff2 yoff)
                           xoff2
                           0)
                         yoff2))))))
        IMeasure
        (measure [this frame x y w h]
          (let [col-width (long (/ w (count els)))]
            (loop [i 0
                   mh 0
                   xoff 0
                   yoff 0
                   wz 0
                   hz 0]
              (if (= i (count els))
                [wz hz]
                (let [[w2 h2] (measure (els i) frame
                                       (+ x xoff)
                                       (+ y yoff)
                                       w h)
                      mh2 (max mh h2)
                      xoff2 (+ xoff w2)
                      yoff2 (if (< xoff2 w) yoff (+ yoff mh2))]
                  (recur (inc i)
                         (if (= yoff2 yoff)
                           mh2
                           0)
                         (if (= yoff2 yoff)
                           xoff2
                           0)
                         yoff2
                         (max wz xoff2)
                         (max hz yoff2)))))))))))

(defn fixed-cols
  [col-width & els]
  (let [els (vec els)]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (loop [cols 0]
          (when (< cols (count els))
            (handle! (els cols) frame (+ x (* cols col-width)) y col-width h)
            (recur (inc cols))))))))

(defn behaviour
  ([f]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (f frame))
     IMeasure
     (measure [this frame x y w h]
       [0 0])))
  ([f & args]
   (behaviour #(apply f % args))))

(defn click-handler
  ([f]
   (if-elem clicked?
     (behaviour f)
     nil-elem))
  ([f & args]
   (click-handler #(apply f % args))))

(defn frame-text
  ([f]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (gdx/draw! (str (f frame)) x y w h))
     IMeasure
     (measure [this frame x y w h]
       (gdx/measure (str (f frame)) w h))))
  ([f & args]
   (frame-text #(apply f % args))))

(defn gs-text
  ([f]
   (frame-text (comp f :game)))
  ([f & args]
   (gs-text #(apply f % args))))

(defmulti scene (comp :scene :game))

(defn hug
  [edges el]
  (let [edges (set edges)]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (let [[w2 h2] (measure el frame x y w h)]
          (-handle! el frame
                    (if (edges :right)
                      (+ x w (- w2))
                      x)
                    (if (edges :bottom)
                      (+ y h (- h2))
                      y)
                    w2
                    h2))))))

(defn min-size
  [w2 h2 el]
  (reify IScreenObject
    (-handle! [this frame x y w h]
      (-handle! el frame x y (max w2 w) (max h h2)))))

(defmulti scene-name identity)

(defmethod scene-name :default
  [k] (str/capitalize (name k)))

(defmacro defscene
  [k & els]
  `(let [el# (min-size 640 480 (stack ~@els))]
     (defmethod scene ~k [_#] el#)))

(defn goto
  [gs scene]
  (-> (assoc gs :scene scene)
      (update :scene-stack (fnil conj []) scene)))

(defn goto-no-follow
  [gs scene]
  (assoc gs :scene scene
            :scene-stack [scene]))

(defn back
  [gs]
  (let [scene-stack (pop (:scene-stack gs))
        scene (peek scene-stack)]
    (if scene
      (assoc gs :scene scene
                :scene-stack scene-stack)
      (goto-no-follow gs :main-menu))))

(def back-handler
  (behaviour
    (fn [frame]
      (when (-> frame :tick :keys-hit (contains? Input$Keys/ESCAPE))
        (swap! state/game back)))))

(defn wrap-opts
  [el opts]
  (let [append (cond-> []
                       (:on-click! opts) (conj (click-handler (:on-click! opts)))
                       (:on-click opts) (conj (click-handler (let [f (:on-click opts)
                                                                   f (if (sequential? f)
                                                                       #(apply (first f) % (rest f))
                                                                       f)]
                                                               (fn [_] (swap! state/game f))))))]
    (if (seq append)
      (apply stack el append)
      el)))

(defn button
  ([el]
   (let [st (stack black (fancy-box 2) (center el))]
     (if-hovering
       (tint Color/YELLOW st)
       st)))
  ([el & {:as opts}]
   (wrap-opts (button el) opts)))

(defn disabled-button
  [el]
  (tint Color/GRAY (stack black (fancy-box 2) (center el))))

(defn selected-button
  ([el]
   (let [st (stack black (fancy-box 2) (center el))]
     (if-hovering
       (tint Color/YELLOW st)
       (tint Color/GREEN st))))
  ([el & {:as opts}]
   (wrap-opts (selected-button el) opts)))

(defn link
  ([el]
   (if-hovering
     (tint Color/YELLOW el)
     el))
  ([el & {:as opts}]
    (wrap-opts (link el) opts)))

(defn selected-link
  ([el]
   (if-hovering
     (tint Color/YELLOW el)
     (tint Color/GREEN el)))
  ([el & {:as opts}]
   (wrap-opts (link el) opts)))

(defn dynamic
  ([f]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (let [el (f frame x y w h)]
          (-handle! el frame x y w h)))
      IMeasure
      (measure [this frame x y w h]
        (let [el (f frame x y w h)]
          (measure el frame x y w h)))))
  ([f & args]
    (dynamic #(apply f % args))))

(defn gs-dynamic
  ([f]
   (dynamic (fn [frame x y w h] (f (:game frame)))))
  ([f & args]
   (gs-dynamic #(apply f % args))))

(def breadcrumbs
  (restrict-height 55
    (pad
      0 6
      (rows
        (center
          (gs-dynamic
            #(apply flow
                    (interpose (resize 12 0
                                 (center "/"))
                               (conj (mapv (fn [s]
                                             (link
                                               (scene-name s)
                                               :on-click [goto-no-follow s]))
                                           (pop (:scene-stack %)))
                                     (selected-link (scene-name (:scene % :main-menu))))))))
        (pad 0 6
             (restrict-height 1 gdx/box1))))))

(def misc
  (gdx/texture (io/resource "tiles/misc.png")))

(def selection-circle
  (gdx/texture-region misc 0 0 32 32))

(def gui
  (gdx/texture (io/resource "tiles/gui.png")))

(def block
  (gdx/texture-region gui 0 0 32 32))