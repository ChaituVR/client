(ns wh.profile.views
  (:require
    [re-frame.core :refer [dispatch dispatch-sync reg-event-db]]
    [wh.components.loader :refer [loader-full-page]]
    [wh.components.not-found :as not-found]
    [wh.logged-in.profile.components :as components]
    [wh.profile.db :as profile]
    [wh.profile.subs :as subs]
    [wh.re-frame.subs :refer [<sub]]))

(defn content []
  (let [profile-hidden? (<sub [::subs/profile-hidden?])
        articles        (<sub [::subs/blogs])
        issues          (<sub [::subs/issues])
        contributions?  (boolean (<sub [::subs/contributions-collection]))]
    (if profile-hidden?
      [components/profile-hidden-message]
      [components/content
       [components/section-stats {:is-owner?      false
                                  :percentile     (<sub [::subs/percentile])
                                  :created        (<sub [::subs/created])
                                  :articles-count (count articles)
                                  :issues-count   (count issues)}]
       [components/section-skills {:type         :public
                                   :skills       (<sub [::subs/skills])
                                   :interests    (<sub [::subs/interests])
                                   :query-params (<sub [:wh/query-params])
                                   :max-skills   profile/maximum-skills}]
       (when contributions?
         [components/contributions-section
          (<sub [::subs/contributions-calendar])
          (<sub [::subs/contributions-count])
          (<sub [::subs/contributions-repos])
          (<sub [::subs/contributions-months])])
       [components/section-articles articles :public]
       [components/section-issues issues :public]])))

(defn page []
  (cond
    (<sub [::subs/error?])
    [not-found/not-found-profile]
    (<sub [::subs/loader?])
    [loader-full-page]
    :else
    [components/container
     [components/profile (<sub [::subs/profile])
      {:twitter       (<sub [::subs/social :twitter])
       :stackoverflow (<sub [::subs/social :stackoverflow])
       :github        (<sub [::subs/social :github])
       :last-seen     (<sub [::subs/last-seen])
       :updated       (<sub [::subs/updated])}
      :public]
     [content]]))
