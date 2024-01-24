package org.migor.feedless.api.auth

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class CurrentUser {

  @Autowired
  lateinit var userService: UserService

  fun isUser(): Boolean = StringUtils.isNotBlank(attr(JwtParameterNames.USER_ID))

  fun user(corrId: String): UserEntity {
    val notFoundException = IllegalArgumentException("user not found ($corrId)")
    return userId()?.let { userService.findById(it).orElseThrow { notFoundException } } ?: throw notFoundException
  }

  fun userId(): UUID? = attr(JwtParameterNames.USER_ID)?.let { UUID.fromString(it) }

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
