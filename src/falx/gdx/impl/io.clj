(ns falx.gdx.impl.io
  (:require [clojure.java.io :as io])
  (:import (com.badlogic.gdx.files FileHandle)
           (java.io File)))

(defn ^FileHandle file-handle
  [file]
  (FileHandle. ^File (io/as-file file)))