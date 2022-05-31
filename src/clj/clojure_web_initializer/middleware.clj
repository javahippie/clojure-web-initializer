(ns clojure-web-initializer.middleware
  (:require
    [clojure-web-initializer.env :refer [defaults]]
    [clojure.tools.logging :as log]
    [clojure-web-initializer.layout :refer [error-page]]
    [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
    [clojure-web-initializer.middleware.formats :as formats]
    [muuntaja.middleware :refer [wrap-format wrap-params]]
    [maja.middleware :refer [honey-middleware]]
    [clojure-web-initializer.tracing :refer [honey]]
    [clojure-web-initializer.config :refer [env]]
    [ring-ttl-session.core :refer [ttl-memory-store]]
    [ring.middleware.defaults :refer [site-defaults wrap-defaults]])
  )

(defn wrap-internal-error [handler]
  (fn [req]
    (try
      (handler req)
      (catch Throwable t
        (log/error t (.getMessage t))
        (error-page {:status  500
                     :title   "Something very bad has happened!"
                     :message "We've dispatched a team of highly trained gnomes to take care of the problem."})))))

(defn wrap-honey-middleware
  "Custom middleware to send traces for every HTTP call to honeycomb.io.
  Uses Maja: https://github.com/lambdaschmiede/maja"
  [handler]
  (if (some? honey)
    (honey-middleware handler honey)
    handler))

(defn wrap-htmx [handler]
  (fn [{:keys [headers] :as req}]
    (handler
      (if (some? (get headers "hx-request"))
        (assoc req :htmx (->> headers
                              (filter (fn [[key _]] (.startsWith key "hx-")))
                              (map (fn [[key val]] [(keyword key) val]))
                              (into {})))
        req))))

(defn wrap-formats [handler]
  (let [wrapped (-> handler wrap-params (wrap-format formats/instance))]
    (fn [request]
      ;; disable wrap-formats for websockets
      ;; since they're not compatible with this middleware
      ((if (:websocket? request) handler wrapped) request))))

(defn wrap-base [handler]
  (-> ((:middleware defaults) handler)
      (wrap-defaults
        (-> site-defaults
            (assoc-in [:security :anti-forgery] false)
            (assoc-in [:session :store] (ttl-memory-store (* 60 30)))))
      wrap-internal-error))
