(ns axo.index
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.dom :as dom :include-macros true]
            [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [cljs-http.client :as http]))

(enable-console-print!)

(defn- add-repo
  [ev]
  (let [el (gdom/getElement "url-input")
        url (.-value el)
        post-url "/app/user/repos"]  
    (go (let [resp (<! (http/post post-url {:json-params {:url url}}))]
          （prn resp))))

(defui Widget
  Object
  (render [this]
    (dom/div nil
      (dom/input #js {:id "url-input" :type "text"})
      (dom/button #js {:onClick add-repo} "submit"))))

(def widget-factory (om/factory Widget))

(js/ReactDOM.render (widget-factory) (gdom/getElement "content"))