package org.migor.feedless.connector.github

data class GithubRepository(
  val name: String,
  val full_name: String,
  val html_url: String,
  val _private: Boolean,
  val description: String?
) {

}
