(ns falx.ui.protocols)

(defprotocol IDraw
  (-draw! [this model rect]))

(defprotocol IDrawLater
  (-drawfn [this rect]))

(defprotocol IHandle
  (-handle [this model input rect]))

(defprotocol IHandleLater
  (-handlefn [this rect]))