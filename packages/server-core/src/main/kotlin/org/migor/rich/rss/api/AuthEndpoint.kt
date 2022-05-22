package org.migor.rich.rss.api

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import org.migor.rich.rss.api.dto.AuthResponseDto
import org.migor.rich.rss.service.AuthService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RestController
class AuthEndpoint {

  private val log = LoggerFactory.getLogger(AuthEndpoint::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @GetMapping("/api/auth")
  fun auth(
    @RequestParam("correlationId", required = false) corrId: String?,
    request: HttpServletRequest
  ): ResponseEntity<AuthResponseDto> {
    try {
      val algorithm: Algorithm = Algorithm.HMAC256("secret")
      val token = JWT.create()
        .withIssuer("rich-rss")
        .sign(algorithm)

      return ResponseEntity.ok(AuthResponseDto(token = token))
    } catch (e: JWTCreationException) {
      log.error(e.message)
    }
    return ResponseEntity.badRequest().build()
  }
}
