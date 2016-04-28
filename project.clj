(defproject falx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.374"]
                 [org.clojure/test.check "0.9.0"]
                 [com.stuartsierra/component "0.3.1"]
                 [com.badlogicgames.gdx/gdx "1.5.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.5.0"]
                 [com.badlogicgames.gdx/gdx-platform "1.5.0"
                  :classifier "natives-desktop"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/data.priority-map "0.0.7"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :main ^:skip-aot falx
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
