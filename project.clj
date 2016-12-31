(defproject falx "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/core.async "0.2.385"]
                 [org.clojure/test.check "0.9.0"]
                 [com.badlogicgames.gdx/gdx "1.5.0"]
                 [com.badlogicgames.gdx/gdx-backend-lwjgl "1.5.0"]
                 [com.badlogicgames.gdx/gdx-platform "1.5.0"
                  :classifier "natives-desktop"]
                 [log4j/log4j "1.2.17"]
                 [org.clojure/data.priority-map "0.0.7"]
                 [ubergraph "0.3.0"]]
  :repositories [["sonatype"
                  "https://oss.sonatype.org/content/repositories/releases/"]]
  :source-paths ["src/clj"
                 "src/java"]
  :target-path "target/%s"
  :main ^:skip-aot falx.main
  :repl-options {:init-ns falx.main}
  :profiles {:uberjar {:aot [falx.main]}})
