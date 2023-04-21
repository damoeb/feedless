package org.migor.rich.rss.auth

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import java.util.*

@Service
class CurrentUser {
  @Autowired
  lateinit var userService: UserService

  fun isUser(): Boolean = StringUtils.isNotBlank(attr(JwtParameterNames.USER_ID))

  fun user(): UserEntity {
    val notFoundException = IllegalArgumentException("user not found")
    return userId()?.let { userService.findById(it).orElseThrow { notFoundException } } ?: throw notFoundException
  }

  fun userId(): UUID? = attr(JwtParameterNames.USER_ID)?.let { UUID.fromString(it) }

  fun isAdmin(): Boolean = false

  private fun attr(param: String): String? {
      return runCatching {
        if (SecurityContextHolder.getContext().authentication is OAuth2AuthenticationToken) {
          StringUtils.trimToNull((SecurityContextHolder.getContext().authentication as OAuth2AuthenticationToken).principal.attributes[param] as String)
        } else {
          null
        }
      }.getOrNull()
  }
}
