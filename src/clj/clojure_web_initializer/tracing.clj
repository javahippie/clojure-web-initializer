(ns clojure-web-initializer.tracing
  (:require
    [clojure-web-initializer.config :refer [env]]
    [maja.core :refer [init-honeycomb wrap-all-trace-methods]]
    [mount.core :refer [defstate]]))

(defstate honey
          :start (let [hn (init-honeycomb (or (:honeycomb env) {}))]
                   (wrap-all-trace-methods hn)
                   hn)
          :stop nil)