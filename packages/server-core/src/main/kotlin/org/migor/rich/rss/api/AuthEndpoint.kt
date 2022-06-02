package org.migor.rich.rss.api

import com.auth0.jwt.exceptions.JWTCreationException
import org.migor.rich.rss.api.dto.AuthResponseDto
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class AuthEndpoint {

  private val log = LoggerFactory.getLogger(AuthEndpoint::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var propertyService: PropertyService

  @GetMapping("/api/auth")
  fun auth(
    @RequestParam("email", required = false) email: String?,
    @RequestParam("corrId", required = false) corrId: String?,
    @CookieValue("XSRF-TOKEN", required = false) csrf: String?,
    request: HttpServletRequest
  ): ResponseEntity<AuthResponseDto> {
    try {
      return ResponseEntity.ok(authService.createAuthToken(csrf, email))
    } catch (e: JWTCreationException) {
      log.error(e.message)
    }
    return ResponseEntity.badRequest().build()
  }
}
