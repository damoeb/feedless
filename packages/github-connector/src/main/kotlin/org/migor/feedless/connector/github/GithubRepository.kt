package org.migor.feedless.connector.github

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class GithubRepository(
//  val name: String,
//  val full_name: String,
//  val html_url: String,
//  val _private: Boolean,
//  val description: String?
  val id: Int,
  val name: String,
  @SerialName("full_name") val fullName: String,
//  @SerialName("private") val isPrivate: String,
//"owner" : {
//  "login" : "damoeb",
//  "id" : 7574272,
//  "node_id" : "MDQ6VXNlcjc1NzQyNzI=",
//  "avatar_url" : "https://avatars.githubusercontent.com/u/7574272?v=4",
//  "gravatar_id" : "",
//  "url" : "https://api.github.com/users/damoeb",
//  "html_url" : "https://github.com/damoeb",
//  "followers_url" : "https://api.github.com/users/damoeb/followers",
//  "following_url" : "https://api.github.com/users/damoeb/following{/other_user}",
//  "gists_url" : "https://api.github.com/users/damoeb/gists{/gist_id}",
//  "starred_url" : "https://api.github.com/users/damoeb/starred{/owner}{/repo}",
//  "subscriptions_url" : "https://api.github.com/users/damoeb/subscriptions",
//  "organizations_url" : "https://api.github.com/users/damoeb/orgs",
//  "repos_url" : "https://api.github.com/users/damoeb/repos",
//  "events_url" : "https://api.github.com/users/damoeb/events{/privacy}",
//  "received_events_url" : "https://api.github.com/users/damoeb/received_events",
//  "type" : "User",
//  "user_view_type" : "public",
//  "site_admin" : false
//},
  val html_url: String,
//  val description: String,
  val url: String,
//"created_at" : "2016-04-20T18:09:26Z",
  val updated_at: String, //"2016-04-20T18:09:27Z",
//"pushed_at" : "2016-03-27T22:29:38Z",
//"git_url" : "git://github.com/damoeb/angular-bootstrap-datetimepicker.git",
//"ssh_url" : "git@github.com:damoeb/angular-bootstrap-datetimepicker.git",
//"clone_url" : "https://github.com/damoeb/angular-bootstrap-datetimepicker.git",
//"svn_url" : "https://github.com/damoeb/angular-bootstrap-datetimepicker",
//"homepage" : "http://dalelotts.github.io/angular-bootstrap-datetimepicker/",
  val size: Int,

//"stargazers_count" : 0,
//"watchers_count" : 0,
//"language" : "JavaScript",
//"has_issues" : false,
//"has_projects" : true,
//"has_downloads" : true,
//"has_wiki" : true,
//"has_pages" : false,
//"has_discussions" : false,
//"forks_count" : 0,
//"mirror_url" : null,
  val archived: Boolean,
  val disabled: Boolean,

//"open_issues_count" : 0,
//"license" : {
//  "key" : "mit",
//  "name" : "MIT License",
//  "spdx_id" : "MIT",
//  "url" : "https://api.github.com/licenses/mit",
//  "node_id" : "MDc6TGljZW5zZTEz"
//},
//"allow_forking" : true,
//"is_template" : false,
//"web_commit_signoff_required" : false,
//"topics" : [ ],
  val visibility: String,
//"forks" : 0,
//"open_issues" : 0,
//"watchers" : 0,
//"default_branch" : "master",
//"permissions" : {
//  "admin" : true,
//  "maintain" : true,
//  "push" : true,
//  "triage" : true,
//  "pull" : true
//}
)


