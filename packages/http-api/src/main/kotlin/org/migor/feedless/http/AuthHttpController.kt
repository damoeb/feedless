package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.auth.AuthUseCase
import org.migor.feedless.http.api.AuthApi
import org.migor.feedless.http.api.model.AuthenticatedUser
import org.migor.feedless.http.mapper.HttpAuthMapper
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Profile("${AppProfiles.user} & ${AppLayer.api}")
class AuthHttpController(
  private val authUseCase: AuthUseCase,
  private val httpAuthMapper: HttpAuthMapper,
) : AuthApi {

  @PreAuthorize("@capabilityService.hasCapability('user')")
  override suspend fun getAuthenticatedUser(): ResponseEntity<AuthenticatedUser> {
    val user = authUseCase.currentUser()
    return ResponseEntity.ok(httpAuthMapper.toHttp(user))
  }
}
