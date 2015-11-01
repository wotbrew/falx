(ns gdx.gl
  (:import (org.lwjgl.opengl GL11)))

(defn clear!
  []
  (GL11/glClearColor 0 0 0 0)
  (GL11/glClear GL11/GL_COLOR_BUFFER_BIT))