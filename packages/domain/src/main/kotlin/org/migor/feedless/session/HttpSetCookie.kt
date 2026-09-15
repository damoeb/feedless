package org.migor.feedless.session

data class HttpSetCookie(
  val name: String,
  val value: String,
  val httpOnly: Boolean = true,
  val maxAge: Int,
  val secure: Boolean = false,
  val path: String = "/",
)
