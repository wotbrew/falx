(ns falx.draw.protocols)

(defprotocol IDraw
  (-draw! [this x y w h]))

(defprotocol IFont
  (-font [this]))

(defprotocol IRecolor
  (-recolor [this color]))

(defprotocol IDrawLater
  (-drawfn [this x y w h]))

(defprotocol IRegionColored)