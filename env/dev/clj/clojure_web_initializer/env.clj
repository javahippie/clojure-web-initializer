(ns clojure-web-initializer.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [clojure-web-initializer.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[clojure-web-initializer started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-web-initializer has shut down successfully]=-"))
   :middleware wrap-dev})
