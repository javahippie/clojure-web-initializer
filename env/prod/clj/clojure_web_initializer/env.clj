(ns clojure-web-initializer.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[clojure-web-initializer started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[clojure-web-initializer has shut down successfully]=-"))
   :middleware identity})
