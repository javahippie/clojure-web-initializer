(ns my-project.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [my-project.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[my-project started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[my-project has shut down successfully]=-"))
   :middleware wrap-dev})
