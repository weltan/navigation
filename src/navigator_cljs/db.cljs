(ns navigator-cljs.db
  (:require [schema.core :as s :include-macros true]))


(def NavigationState
  {:key s/Keyword
   :title s/Str
   s/Keyword s/Any})

(def NavigationParentState
  (dissoc
    (merge NavigationState
           {:index    s/Int
            :children [NavigationState]})
    :title))

(def Message
  {:name s/Str
   :text s/Str
   :image s/Any
   :position s/Str
   :date s/Any
   (s/optional-key :view) s/Any
   :uniqueId s/Int})

;; schema of app-db
(def schema {:nav NavigationParentState
             :messages [Message]})
;; initial state of app-db
(def app-db {:nav {:index    0
                   :key      :home
                   :children [{:key :login-route
                               :title "Balboa Login"}]}
             :messages [{:text "are you building a chat app?"
                         :name "React Native"
                         :image nil
                         :position "left"
                         :date (js/Date. 2015 0 16 19 0)
                         :uniqueId 1}
                        {:text "yessir, I am indeedy!"
                         :name "Developer"
                         :image nil
                         :position "right"
                         :date (js/Date. 2015 0 16 19 1)
                         :uniqueId 2}
                        {:text "pretty cool bro, pretty cool."
                         :name "React Native"
                         :image nil
                         :position "left"
                         :date (js/Date. 2015 0 16 19 2)
                         :uniqueId 3}]})
