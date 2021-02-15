(defproject clojure-energy "0.1.0"
  :description "ClojureScript SPA for sorting words manually"
  :url "https://github.com/ljpengelen/clojure-energy"
  :license {:name "MIT License"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.2"]
                 [org.clojure/clojurescript "1.10.764"]
                 [org.clojure/core.async "1.3.610"]
                 [reagent "1.0.0"]]

  :source-paths ["src"]

  :aliases {"fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" "clojure-energy.test-runner"]
            "fig:ci"    ["run" "-m" "figwheel.main" "-co" "ci.cljs.edn" "-m" "clojure-energy.test-runner"]}

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.2.12"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]]}})
