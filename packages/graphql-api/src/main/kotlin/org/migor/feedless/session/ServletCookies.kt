package org.migor.feedless.session

import jakarta.servlet.http.Cookie

internal fun toServletCookie(cookie: HttpSetCookie): Cookie {
  return Cookie(cookie.name, cookie.value).apply {
    isHttpOnly = cookie.httpOnly
    maxAge = cookie.maxAge
    secure = cookie.secure
    path = cookie.path
  }
}
