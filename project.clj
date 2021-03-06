(defproject feedly-membrane "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.2-alpha1"]
                 [com.phronemophobic/membrane "0.9.14-beta"]
                 [re-frame "1.0.0-rc3"]
                 [com.googlecode.lanterna/lanterna "3.0.2"]
                 [org.clojars.sids/htmlcleaner "2.1"]
                 [defun "0.3.1"]
                 [commons-lang "2.5"]]
  ;:jvm-opts ["-Dclojure.compiler.direct-linking=true"]
  :aliases
  {"native"
   ["shell"
    "native-image" "--report-unsupported-elements-at-runtime"
    "--initialize-at-build-time" "--no-server" "--no-fallback"
    "-jar" "./target/uberjar/${:uberjar-name:-${:name}-${:version}-standalone.jar}"
    "-H:Name=./target/${:name}"]}
  :main ^:skip-aot feedly-membrane.view
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-shell "0.5.0"]
                             [com.jakemccrary/lein-test-refresh "0.24.1"]]}})
