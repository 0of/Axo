(ns axo.index
  (:require-macros [cljs.core.async.macros :refer [go alt!]])
  (:require [cljs.core.async :refer [put! <! >! chan timeout]]
            [om.dom :as dom :include-macros true]
            [om.next :as om :refer-macros [defui]]
            [goog.dom :as gdom]
            [cljs-http.client :as http]))

(enable-console-print!)

(defui Widget
  Object
  (render [this]
    (dom/div nil
      (dom/input #js {:type "text" :ref "repo-url"})
      (dom/button nil "Add"))))

(def widget-factory (om/factory Widget))

(js/ReactDOM.render (widget-factory) (gdom/getElement "content"))