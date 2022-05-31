(ns htmx-test.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[htmx-test started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[htmx-test has shut down successfully]=-"))
   :middleware identity})
