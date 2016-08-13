(ns falx.engine.ui.protocols)

(defprotocol IDraw
  (-draw! [this model input rect]))

(defprotocol IDrawLater
  (-drawfn [this rect]))

(defprotocol IHandle
  (-handle [this model input rect]))

(defprotocol IHandleLater
  (-handlefn [this rect]))