(defproject falx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.badlogicgames.gdx/gdx "1.5.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.5.0"]
                 [com.badlogicgames.gdx/gdx-platform "1.5.0"
                  :classifier "natives-desktop"]
                 [log4j/log4j "1.2.17"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :main ^:skip-aot falx
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
