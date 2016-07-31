(ns falx.ui.protocols)

(defprotocol IDraw
  (-draw! [this view rect]))

(defprotocol IDrawLater
  (-drawfn [this rect]))

(defprotocol IHandle
  (-handle [this gs rect]))

(defprotocol IHandleLater
  (-handlefn [this rect]))