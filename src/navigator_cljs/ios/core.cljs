(ns navigator-cljs.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [navigator-cljs.handlers]
            [navigator-cljs.subs]))

(def react-native (js/require "react-native"))
(def gifted-messenger (js/require "react-native-gifted-messenger"))

(def app-registry (.-AppRegistry react-native))
(def text (r/adapt-react-class (.-Text react-native)))
(def text-input (r/adapt-react-class (.-TextInput react-native)))
(def view (r/adapt-react-class (.-View react-native)))
(def image (r/adapt-react-class (.-Image react-native)))
(def list-view (r/adapt-react-class (.-ListView react-native)))
(def touchable-highlight (r/adapt-react-class (.-TouchableHighlight react-native)))
(def card-stack (r/adapt-react-class (.-CardStack (.-NavigationExperimental react-native))))
(def navigation-header-comp (.-Header (.-NavigationExperimental react-native)))
(def navigation-header (r/adapt-react-class navigation-header-comp))
(def header-title (r/adapt-react-class (.-Title (.-Header (.-NavigationExperimental react-native)))))
(def gifted-messenger-component (r/adapt-react-class gifted-messenger))

(def logo-img (js/require "./images/logo.png"))

(def style
  {:view        {:flex-direction "column"
                 :margin         40
                 :margin-top     (.-HEIGHT navigation-header-comp)
                 :align-items    "center"}
   :image       {:width         100
                 :height        100
                 :margin-bottom 20
                 :margin-top 20}
   :title       {:font-size     40
                 :font-weight   "100"
                 :margin-bottom 20
                 :text-align    "center"}
   :button-text {:color       "white"
                 :text-align  "center"
                 :font-weight "bold"}
   :button      {:background-color "#db4f37"
                 :padding          10
                 :margin-bottom    10
                 :border-radius    5
                 :align-self "stretch"}
   :text-input {:height 40
                :background-color "#fff"
                :margin-bottom 20
                :padding 10}
   :input-prompt {:font-size 12
                  :color "#000"
                  :font-weight "100"
                  :align-self "flex-start"}
   :view-container {:margin-top (.-HEIGHT navigation-header-comp)}
   :preview-container {:padding 10
                       :background-color "#f6f6f6"}
   :row {:flex-direction "row"
         :height 30
         :padding 5}
   :email {:font-weight "bold"
           :flex 1}
   :date {:color "#ccc"
          :font-size 13}
   :message-preview {:color "#bbb"}
   :separator {:height 1
               :background-color "#cccccc"}})

(defn nav-title [props]
  [header-title (aget props "scene" "navigationState" "title")])

(defn header
  [props]
  [navigation-header
   (assoc
     (js->clj props)
     :render-title-component #(r/as-element (nav-title %)))])

(defn login-scene []
  [view {:style (:view style)}
   [image {:source logo-img
           :style  (:image style)}]
   [text {:style (:title style)} "Balboa Login"]
   [text {:style (:input-prompt style)} "Email"]
   [text-input {:style (:text-input style)
                :placeholder "Enter your email address"}]
   [text {:style (:input-prompt style)} "Password"]
   [text-input {:style (:text-input style)
                :placeholder "Enter your password"}]
   [touchable-highlight
    {:style    (:button style)
     :on-press #(dispatch [:nav/push {:key   :chat-table-route
                                      :title "Chats"}])}
    [text {:style (:button-text style)} "Login"]]
   [touchable-highlight
    {:style    (:button style)
     :on-press #(dispatch [:nav/home nil])}
    [text {:style (:button-text style)} "Create an Account"]]])

(defn chat-view []
  (let [messages (subscribe [:messages])]
    (fn []
      [gifted-messenger-component {:styles {:bubble-right {:background-color "#db4f37"}}
                                   :style (:view-container style)
                                   :messages @messages
                                   :handle-send #(dispatch [:chat/send %])}])))

(def data-source (react-native.ListView.DataSource.
                   (clj->js {:rowHasChanged (fn [r1 r2] (not= r1 r2))})))

(defn row-component [props]
  (let [{:keys [row]} props]
    [touchable-highlight {:on-press #(dispatch [:nav/push {:key :chat-route
                                                           :title (str "Chat with " row)}])}
     [view {:style (:preview-container style)}
      [view {:style (:row style)}
       [text {:style (:email style)} row]
       [text {:style (:date style)} "1/1/12"]]
      [view {:style (:row style)}
       [text {:style (:message-preview style)} "Test Message."]]]]))

(defn separator [props]
  (let [{:keys [row-id section-id]} props]
    [view {:key (str section-id row-id)
           :style (:separator style)}]))

(defn chat-table-view []
  [list-view {:style (:view-container style)
              :dataSource (.cloneWithRows data-source (clj->js ["Alice Svenn (alicesvenn@gmail.com)" "Bob Landon (boblandon4@gmail.com)"]))
              :render-row (fn [row]
                            (r/create-element (r/reactify-component row-component) #js{:row row
                                                                                       :key row}))
              :renderSeparator (fn [section-id row-id]
                                 (r/create-element (r/reactify-component separator) #js{:row-id row-id
                                                                                        :key row-id
                                                                                        :section-id section-id}))}])

; get scene info via props->scene->navigationState, rather than :nav/current,
; because if you use :nav/current, it switches scenes before the animation happens
(defn scene [props]
  (let [idx (aget props "scene" "index")
        current-key (keyword (aget props "scene" "navigationState" "key"))
        next-title (str "Route " (inc idx))
        next-key (keyword (str idx))]
    (case current-key
      :login-route [login-scene]
      :chat-table-route [chat-table-view]
      :chat-route [chat-view]
      [login-scene])))

(defn app-root []
  (let [nav (subscribe [:nav/state])]
    (fn []
      [card-stack {:on-navigate      #(dispatch [:nav/pop nil])
                   :render-overlay   #(r/as-element (header %))
                   :navigation-state @nav
                   :style            {:flex 1}
                   :render-scene     #(r/as-element (scene %))}])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "NavigatorCljs" #(r/reactify-component app-root)))
