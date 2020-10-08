(ns wh.profile.events
  (:require
    [re-frame.core :refer [dispatch path reg-event-db reg-event-fx]]
    [wh.db :as db]
    [wh.graphql-cache :refer [reg-query] :as gql-cache]
    #?(:cljs [wh.pages.core :as pages]))
  (#?(:clj :require :cljs :require-macros)
   [wh.graphql-macros :refer [defquery]]))

(defquery profile-query
  {:venia/operation {:operation/type :query
                     :operation/name "fetch_user"}
   :venia/variables [{:variable/name "id"
                      :variable/type :ID!}]
   :venia/queries   [[:user {:id :$id}
                      [[:skills [:name :rating
                                 [:tag :fragment/tagFields]]]
                       [:interests :fragment/tagFields]
                       [:otherUrls [:url]]
                       :imageUrl
                       :name
                       :id
                       :summary
                       :percentile
                       :published
                       :created
                       :lastSeen
                       :updated

                       [:contributionsCollection
                        [:totalCommitContributions
                         :totalRepositoriesWithContributedCommits
                         [:contributionCalendar
                          [[:weeks
                            [[:contributionDays
                              [:contributionCount
                               :date :weekday :color]]]]]]]]]]

                     [:blogs {:user_id :$id}
                      [[:blogs [:id :title :formattedCreationDate
                                :readingTime :upvoteCount :published]]]]

                     [:query_issues {:user_id :$id}
                      [[:issues [:id :title :level
                                 [:compensation [:amount :currency]]
                                 [:company [:id :name :logo :slug]]
                                 [:repo [:primary_language]]]]]]]})

(reg-query :profile profile-query)

(defn profile-query-description [db]
  [:profile {:id (get-in db [:wh.db/page-params :id])}])

(reg-event-fx
  ::set-page-title
  db/default-interceptors
  (fn [{db :db} _]
    {:page-title {:page-name (some-> (apply gql-cache/result db (profile-query-description db)) :user :name)
                  :vertical  (:wh.db/vertical db)}}))

(reg-event-fx
  ::load-profile
  db/default-interceptors
  (fn [{db :db} _]
    {:dispatch (into [:graphql/query]
                     (conj (profile-query-description db)
                           {:on-complete [::set-page-title]
                            :on-success  [:wh.events/scroll-to-top]}))}))

#?(:cljs
   (defmethod pages/on-page-load :user [_]
     [[:wh.pages.core/unset-loader]
      [::load-profile]]))
