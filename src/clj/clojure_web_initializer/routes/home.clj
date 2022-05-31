(ns clojure-web-initializer.routes.home
  (:require
    [clojure-web-initializer.layout :as layout]
    [clojure-web-initializer.middleware :as middleware]
    [ring.util.response :refer [file-response]]
    [hiccup.core :refer [html]]
    [ring.util.http-response :as response]
    [clojure.set :refer [index]]
    [clojure.java.shell :refer [sh]]))

(def modules #{
               {:name        "aleph"
                :category    :server
                :description "adds the Aleph server"}
               {:name        "http-kit"
                :category    :server
                :description "adds the fast HTTP Kit web server to the project"}
               {:name        "immutant"
                :category    :server
                :description "adds the immutant web server to the project. Note: this project is no longer funded/maintained"}
               {:name        "jetty"
                :category    :server
                :description "adds the jetty web server to the project"}
               {:name        "undertow"
                :category    :server
                :description "adds the ring-undertow server. This is a default server."}

               {:name        "h2"
                :category    :database
                :description "adds db.core namespace and H2 database dependencies"}
               {:name        "postgres"
                :category    :database
                :description "adds db.core namespace and PostgreSQL database dependencies"}
               {:name        "mysql"
                :category    :database
                :description "adds db.core namespace and MySQL/MariaDB database dependencies"}
               {:name        "mongodb"
                :category    :database
                :description "adds support for MongoDB using the Monger library"}
               {:name        "datomic"
                :category    :database
                :description "adds support for the Datomic database"}
               {:name        "sqlite"
                :category    :database
                :description "adds support for the SQLite database"}
               {:name        "xtdb"
                :category    :database
                :description "adds support for the XTDB database"}

               {:name        "graphql"
                :category    :service
                :description "adds GraphQL support using Lacinia"}
               {:name        "swagger"
                :category    :service
                :description "adds support for Swagger-UI"}
               {:name        "service"
                :category    :service
                :description "removes static assets and the layout, adds Swagger support"}

               {:name        "cljs"
                :category    :cljs
                :description "adds ClojureScript support to the project"}
               {:name        "reagent"
                :category    :cljs
                :description "adds ClojureScript support with Reagent to the project along with an example"}
               {:name        "re-frame"
                :category    :cljs
                :description "adds ClojureScript support with re-frame to the project along with an example"}
               {:name        "kee-frame"
                :category    :cljs
                :description "adds kee-frame to the project"}
               {:name        "shadow-cljs"
                :category    :cljs
                :description "adds shadow-cljs support to the project, replacing the default cljsbuild and figwheel setup"}

               {:name        "boot"
                :category    :misc
                :description "causes the project to run with Boot instead of Leiningen"}
               {:name        "auth"
                :category    :misc
                :description "adds Buddy dependency and authentication middleware"}
               {:name        "auth-jwe"
                :category    :misc
                :description "adds Buddy dependency with the JWE backend"}
               {:name        "oauth"
                :category    :misc
                :description "adds OAuth dependency"}
               {:name        "hoplon"
                :category    :misc
                :description "adds ClojureScript support with Hoplon to the project"}
               {:name        "cucumber"
                :category    :misc
                :description "adds support for browser based UI testing with Cucumber and clj-webdriver"}
               {:name        "sassc"
                :category    :misc
                :description "adds support for SASS/SCSS files using SassC command line compiler"}
               {:name        "war"
                :category    :misc
                :description "adds support of building WAR archives for deployment to servers such as Apache Tomcat (should NOT be used for Immutant apps running on WildFly)"}
               {:name        "site"
                :category    :misc
                :description "creates template for site using the specified database (H2 by default) and ClojureScript"}
               {:name        "kibit"
                :category    :misc
                :description "adds lein-kibit plugin"}
               {:name        "servlet"
                :category    :misc
                :description "adds middleware for handling Servlet context"}
               {:name        "basic"
                :category    :misc
                :description "generates a bare bones luminus project"}})


(defn home-page {:traced true} [request]
  (layout/render request "base.html"))

(defn search-modules {:traced true} [{:keys [params]}]
  (let [categories (->> modules
                        (filter (fn [{:keys [name]}]
                                  (if (:module-query params)
                                    (.startsWith name (:module-query params))
                                    true)))
                        (filter (fn [{:keys [category]}]
                                  (if (:moduleSelect params)
                                    (or (= "all" (:moduleSelect params))
                                        (= category (keyword (:moduleSelect params))))
                                    true))))]
    (response/ok (html [:ul {:class "list-group module-list"}
                        (for [[k v] (index categories [:category])]
                          (for [{:keys [name category description]} v]
                            [:li
                             {:class     "list-group-item d-flex justify-content-between align-items-center"
                              :hx-post   (str "/add?value=" name)
                              :hx-target "#modules"
                              :hx-swap   "innerHtml"}
                             [:div
                              [:div {:class "fw-bold"} name]
                              [:span description]]
                             [:span {:class "badge bg-primary rounded-pill"} category]]))]))))

(defn render-module {:traced true} [module]
  [:span {:class     "badge rounded-pill text-bg-primary close"
          :hx-delete (str "/remove?value=" module)
          :hx-target "#modules"
          :hx-swap   "innerHtml"} module])

(defn add-module {:traced true} [{:keys [session params]}]
  (let [s (update session :modules #(conj (set %) (:value params)))]
    (assoc (response/ok (html (map render-module (:modules s)))) :session s)))

(defn remove-module {:traced true} [{:keys [session params]}]
  (let [s (update session :modules disj (:value params))]
    (assoc (response/ok (html (map render-module (:modules s)))) :session s)))

(defn generate-template {:traced true} [{:keys [session params]}]
  (let [id (str "/tmp/cljgen_" (random-uuid))
        name (:name params)
        modules (map #(str "+" %) (:modules session))]
    (sh "mkdir" id)
    (apply sh (concat ["lein" "new" "luminus" name] modules [:dir id]))
    (sh "zip" "-r" (str name ".zip") name :dir id)
    (update-in (file-response (str id "/" (str name ".zip")))
               [:session] dissoc modules)))

(comment
  (index modules [:category])
  (search-modules "h"))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-honey-middleware
                 middleware/wrap-htmx
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/search" {:post search-modules}]
   ["/generate.zip" {:post generate-template}]
   ["/add" {:post add-module}]
   ["/remove" {:delete remove-module}]])

