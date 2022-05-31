(ns my-project.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[my-project started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[my-project has shut down successfully]=-"))
   :middleware identity})
