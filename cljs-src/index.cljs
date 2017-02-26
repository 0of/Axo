(ns axo.index
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.dom :as dom :include-macros true]
            [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [goog.json :as json]
            [cljs-http.client :as http])
  (:import [goog.net WebSocket]))

(enable-console-print!)

(defn- add-repo
  [ev]
  (let [el (gdom/getElement "url-input")
        url (.-value el)
        post-url "/app/user/repos"]
    (go (let [resp (<! (http/post post-url {:json-params {:url url}}))]
          （prn resp))))

(defn- add-repo-via-ws
  [ev ws]
  (let [el (gdom/getElement "url-input")
        url (.-value el)
        post-url "POST /app/user/repos"
        body (json/serialize {:method post-url
                              :body {:url url}})]
    (go (.send ws body))))

(defui RepoList
  Object
  (componentDidMount [this]
    (go (let [resp (<! (http/get "/app/user/repos" nil))]
          (if (= 200 (:status resp))
            (om/set-state! this (:body resp))))))

  (render [this]
    (let [{:keys [repos]} (om/get-state this)]
      (dom/ul nil
        (map #(dom/li nil (dom/a nil %)) repos)))))

(def repolist-factory (om/factory RepoList))

(defui Widget
  Object
  (componentDidMount [this]
    ;; connect WS
    (let [ws (WebSocket.)]
      (.open ws "ws://:8991")
      ;; register event handler
      (.listenOnce ws (.. WebSocket -Event -OPENED) #(om/set-state! this {:ws ws}))
      ;; register event handler TODO
      (.listen ws (.. WebSocket -Event -MESSAGE #()))))

  (render [this]
    (let [{:keys [ws] (om/get-state this)}]
      (dom/div nil
        (repolist-factory)
        (dom/input #js {:id "url-input" :type "text"})
        (dom/button #js {:onClick #(add-repo % ws) "submit"})))))

(def widget-factory (om/factory Widget))

(js/ReactDOM.render (widget-factory) (gdom/getElement "content"))

