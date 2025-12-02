package org.migor.feedless.user

data class UserCreate(
  val email: String? = null,
  val githubId: String? = null,
  val firstName: String? = null,
  val lastName: String? = null,
  val country: String? = null,
  val hasAcceptedTerms: Boolean = false,
)
