package org.migor.feedless.http.mapper

import org.migor.feedless.auth.AuthenticatedUser
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.AuthenticatedUser as HttpAuthenticatedUser

@Component
class HttpAuthMapper(
  private val groupMapper: HttpGroupMapper,
) {

  fun toHttp(user: AuthenticatedUser): HttpAuthenticatedUser =
    HttpAuthenticatedUser(
      id = user.id.uuid,
      email = user.email,
      groups = user.groups.map { groupMapper.toHttp(it) },
    )
}
