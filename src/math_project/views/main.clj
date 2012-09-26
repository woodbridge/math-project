(ns math-project.views.main
  (require [redis.core :as redis])
  (:require [math-project.views.common :as common])
  (:require [noir.request :as request]
            [noir.response :as resp]
            [noir.session :as session])
  (:use [noir.core :only [defpage]]
        [hiccup.page :only [html5]]
        [hiccup.core :only [html]]))

(def redis-config {:host "127.0.0.1" :port 6379 :db 0})

(defn log-choice [choice]
  (redis/with-server redis-config
    (redis/incr choice)))

(defn stats  []
  (redis/with-server
    redis-config
    (do
      (let [raw {:a (read-string (redis/get "a"))
                 :b (read-string  (redis/get "b"))}
            total (+ (:a raw) (:b raw))
            stats-map {:a-percent (float (/ (:a raw) total)) 
                       :b-percent (float (/ (:b raw) total))
                       :total total}]
        (merge raw stats-map)))))

(defn pjax? []
  ((:headers (request/ring-request))) "x-pjax")

(defn game []
  (html
   [:section#a.choice
    [:h2 "A"]
    [:img.a {:src "/img/a.jpeg" :data-choice "a"}]]
   [:section#b.choice
    [:h2 "B"]
    [:img.b {:src "/img/a.jpeg" :data-choice "b"}]]))

;; Routes

(defpage "/" []
  (if (session/get :used)
    (resp/redirect "/stats")
    (common/layout
     [:section#game
      [:h2 "The Rules"]
      [:ol#rule-list
       [:li "Choose picture "
        [:strong.a "A"]
        " or "
        [:strong.b "B"]]
       [:li "Be nice."]]]
     [:a#begin {:href "/game" :data-pjax "#rules"} "Begin!"] )))


(defpage "/game" []
  (let [headers (:headers (request/ring-request))]
    (if (headers "x-pjax")
      (game)
      (common/layout (game)))))

(defpage "/stats" []
  (let [results (stats)]
    (common/layout
     [:h1 "Thank you for playing!"]
     [:section#stats-results
      [:h2.a-results (str (Math/round (* 100 (:a-percent results)))) "% of people so far chose A"]
      [:h2.b-results (str (Math/round (* 100 (:b-percent results)))) "% of people so far chose B"]
      [:p.total (str (:total results) " people have played.  Please send it around.")]
      [:form {:id "why"}
       [:textarea
        {:placeholder "Care to explain your choice?"}]
       [:button "Send"]]]
     [:section#faq
      [:h2 "FAQ"]
      [:h3 "That was very short"]
      [:p "Yes."]
      [:h3 "Is this anynomous?"]
      [:p "Yes."]
      [:h3 "Can I see the code?"]
      [:a {:href "https://github.com/woodbridge/math-project"} "Yes."]])))

(defpage [:post "/choose"] {:keys [choice]}
  (session/put! :used "true")
  (let [params (:params  (request/ring-request))]
    (let [choice (:choice params)]
      (log-choice choice)
      (resp/json {:status "OK"
                  :choice choice}))))

(defpage [:post "/thoughts"] {}
  (let [params (:params (request/ring-request))]
    (let [thoughts (:text params)]
      (redis/with-server redis-config
        (redis/lpush "thoughts" thoughts))
      (resp/json {:status "ok"}))))