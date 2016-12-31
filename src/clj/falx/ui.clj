(ns falx.ui
  (:require [falx.gdx :as gdx]
            [clojure.java.io :as io]
            [falx.ui.protocols :refer :all]
            [clojure.string :as str]
            [falx.game :as g]
            [falx.frame :as frame])
  (:import (com.badlogic.gdx.graphics Color)
           (com.badlogic.gdx Input$Buttons Input$Keys)))

(extend-protocol IScreenObject
  nil
  (-handle! [this frame x y w h])
  Object
  (-handle! [this frame x y w h]
    (gdx/draw-in! this x y w h)))

(extend-protocol IMeasure
  nil
  (measure [this frame x y w h]
    [0 0])
  Object
  (measure [this frame x y w h]
    [w h])
  String
  (measure [this frame x y w h]
    (gdx/measure this w h)))

(defn handle!
  ([obj frame]
   (let [[w h] (-> frame frame/screen-size)]
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
        (if-some [o (get m k)]
          (measure o frame x y w h)
          [0 0])))))


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
     (f (:state frame))))
  ([f & args]
   (gs-pred #(apply f % args))))

(defn if-hovering
  ([then]
   (if-hovering then nil-elem))
  ([then else]
   (if-elem frame/mouse-in?
     then else)))

(defn resize
  ([loc el]
   (let [[w h] loc]
     (resize w h el)))
  ([w2 h2 el]
   (reify IScreenObject
     (-handle! [this frame x y _ _]
       (-handle! el frame x y w2 h2))
     IMeasure
     (measure [this frame _ _ _ _]
       [w2 h2]))))

(defn restrict-width
  ([w2 el]
   (reify IScreenObject
     (-handle! [this frame x y _ h]
       (-handle! el frame x y w2 h))
     IMeasure
     (measure [this frame x y _ h]
       (let [[_ h2] (measure el frame x y w2 h)]
         [w2 h2])))))

(defn restrict-height
  ([h2 el]
   (reify IScreenObject
     (-handle! [this frame x y w _]
       (-handle! el frame x y w h2))
     IMeasure
     (measure [this frame x y w _]
       (let [[w2 _] (measure el frame x y w h2)]
         [w2 h2])))))

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
       (measure el frame (+ x x2) (+ y y2) (- w x2 x2) (- h y2 y2)))))
  ([x2 y2 el & els]
   (pad x2 y2 (apply stack el els))))

(defn shift
  ([x2 y2 el]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! el frame (+ x x2) (+ y y2) (- w x2) (- h y2)))
     IMeasure
     (measure [this frame x y w h]
       (measure el frame (+ x x2) (+ y y2) (- w x2) (- h y2)))))
  ([x2 y2 el & els]
   (shift x2 y2 (apply stack el els))))

(defn translate
  ([x2 y2 el]
   (reify IScreenObject
     (-handle! [this frame x y w h]
       (-handle! el frame (+ x x2) (+ y y2) w h))
     IMeasure
     (measure [this frame x y w h]
       (measure el frame (+ x x2) (+ y y2) w h))))
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
          (loop [i 0
                 mh 0
                 xoff 0
                 yoff 0]
            (when (< i (count els))
              (let [wl (- w xoff)
                    hl (- h yoff)
                    [w2 h2] (measure (els i) frame
                                     (+ x xoff)
                                     (+ y yoff)
                                     wl hl)
                    xoff2 (+ xoff w2)
                    mh2 (max mh h2)
                    yoff2 (if (<= xoff2 w) yoff (+ yoff mh2))]
                (cond
                  (= yoff2 yoff)
                  (do
                    (handle! (els i) frame (+ x xoff) (+ y yoff) w2 h2)
                    (recur (inc i)
                           mh2
                           xoff2
                           yoff2))
                  (<= (+ yoff2 h2) h)
                  (do
                    (handle! (els i) frame x (+ y yoff2) w2 h2)
                    (recur (inc i)
                           0
                           0
                           yoff2))
                  :else nil)))))
        IMeasure
        (measure [this frame x y w h]
          (loop [i 0
                 mh 0
                 xoff 0
                 yoff 0
                 wz 0
                 hz 0]
            (if-not (< i (count els))
              [wz hz]
              (let [wl (- w xoff)
                    hl (- h yoff)
                    [w2 h2] (measure (els i) frame
                                     (+ x xoff)
                                     (+ y yoff)
                                     wl hl)
                    mh2 (max mh h2)
                    xoff2 (+ xoff w2)
                    yoff2 (if (<= xoff2 w) yoff (+ yoff mh2))]
                (cond
                  (= yoff2 yoff)
                  (recur (inc i)
                         mh2
                         xoff2
                         yoff2
                         (max wz xoff2)
                         (max hz (+ yoff h2)))
                  (<= (+ yoff2 h2) h)
                  (recur (inc i)
                         0
                         0
                         yoff2
                         (max wz xoff2)
                         (max hz (+ yoff2 h2)))
                  :else [wz hz])))))))))

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

(defn gs-behaviour
  ([f]
   (behaviour (fn [frame] (g/update-state! (:game frame) f))))
  ([f & args]
   (gs-behaviour #(apply f % args))))

(defn click-handler
  ([f]
   (if-elem frame/clicked?
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
   (frame-text (comp f :state)))
  ([f & args]
   (gs-text #(apply f % args))))

(defmulti scene (comp :scene :state))

(defn hug
  [edges el]
  (let [edges (set edges)]
    (reify IScreenObject
      (-handle! [this frame x y w h]
        (let [[w2 h2] (measure el frame x y w h)]
          (-handle! el frame
                    (if (edges :right)
                      (+ x (- w w2))
                      x)
                    (if (edges :bottom)
                      (+ y (- h h2))
                      y)
                    w
                    h)))
      IMeasure
      (measure [this frame x y w h]
        (measure el frame x y w h)))))

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

(defn goto-pop
  [gs scene]
  (loop [gs gs
         s (:scene gs)
         st (:scene-stack gs)]
    (if (= s scene)
      (assoc gs :scene s :scene-stack (conj st s))
      (if-some [s2 (peek st)]
        (recur gs s2 (pop st))
        (goto gs scene)))))

(defn back
  [gs]
  (let [scene-stack (pop (:scene-stack gs))
        scene (peek scene-stack)]
    (if scene
      (assoc gs :scene scene
                :scene-stack scene-stack)
      (goto-no-follow gs :main-menu))))

(defrecord Down [k])

(defn down
  [k]
  (->Down k))

(defn key-combo-pred
  [& ks]
  (fn [frame _ _ _ _]
    (every? (fn ! [k] (cond
                      (vector? k) (every? ! k)
                      (set? k) (some ! k)
                      (instance? Down k) (-> frame :tick :keys-down (contains? (:k k)))
                      :else (-> frame :tick :keys-hit (contains? k))))
            ks)))

(def back-handler
  (if-elem (key-combo-pred Input$Keys/ESCAPE)
    (gs-behaviour back)))

(def hover-over-ref (atom "fred"))

(def translucent
  (Color. 0 0 0 0.8))

(def hover-over
  (reify IScreenObject
    (-handle! [this frame _ _ _ _]
      (when-some [contents @hover-over-ref]
        (let [[x y] (:mouse-loc (:tick frame))
              [w2 h2] (gdx/measure contents 256 256)
              bx (+ x 6)
              by (+ y 6)]
          (gdx/with-color
            translucent
            (gdx/draw! gdx/pixel x y (+ w2 12) (+ h2 12)))
          (gdx/with-color
            Color/YELLOW
            (draw-fancy-box! x y (+ w2 12) (+ h2 12)))
          (handle! contents frame bx by w2 h2))
        (reset! hover-over-ref nil)))))

(defn wrap-opts
  [el opts]
  (let [append (cond-> []
                       (:hover-over opts)
                       (conj
                         (let [hovering-for (volatile! 0.0)]
                           (if-hovering
                             (behaviour
                               (fn [frame]
                                 (vswap! hovering-for + (:delta (:tick frame)))
                                 (when (< 0.5 @hovering-for)
                                   (reset! hover-over-ref
                                           (:hover-over opts)))))
                             (behaviour
                               (fn [_]
                                 (vreset! hovering-for 0.0))))))
                       (:on-click! opts)
                       (conj (click-handler
                               (:on-click! opts)))
                       (:on-click opts)
                       (conj (click-handler
                               (let [f (:on-click opts)
                                     f (if (sequential? f)
                                         #(apply (first f) % (rest f))
                                         f)]
                                 (fn [frame]
                                   (g/update-state! (:game frame) f))))))]
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

(def vlight-gray
  (Color. 0.9 0.9 0.9 1.0))

(defn link
  ([el]
   (if-hovering
     (tint Color/YELLOW el)
     (tint vlight-gray el)))
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
   (dynamic (fn [frame x y w h] (f (:state frame)))))
  ([f & args]
   (gs-dynamic #(apply f % args))))

(def breadcrumbs
  (restrict-height 55
    (rows
      (center
        (gs-dynamic
          #(apply flow
                  (interpose (resize 12 0
                               (center "/"))
                             (conj (mapv (fn [s]
                                           (link
                                             (scene-name s)
                                             :on-click [goto-pop s]))
                                         (pop (:scene-stack %)))
                                   (selected-link (scene-name (:scene % :main-menu))))))))
      (pad 0 12
           (restrict-height 1 gdx/box1)))))

(defn cycler
  [getfn setfn left right]
  (stack
    (restrict-width
      24
      (if-elem (gs-pred left)
        (button "<"
          :on-click (fn [gs] (setfn gs (left gs))))
        (disabled-button "<")))
    (stack
      (fancy-box 2)
      (center
        (gs-text (comp str getfn))))
    (hug #{:right}
      (restrict-width 24
        (if-elem (gs-pred right)
          (button ">"
            :on-click
            (fn [gs] (setfn gs (right gs))))
          (disabled-button ">"))))))

(defn if-debug
  ([then]
    (if-debug then nil-elem))
  ([then else]
   (if-elem (key-combo-pred (down Input$Keys/F2))
     then
     else)))

(def misc
  (gdx/texture (io/resource "tiles/misc.png")))

(def selection-circle
  (gdx/texture-region misc 0 0 32 32))

(def gui
  (gdx/texture (io/resource "tiles/gui.png")))

(def block
  (gdx/texture-region gui 0 0 32 32))
