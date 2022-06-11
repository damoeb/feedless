package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.AuthResponseDto
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.PropertyService
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

  @Autowired
  lateinit var propertyService: PropertyService

  @Throttled
  @GetMapping("/api/auth")
  fun auth(
    @RequestParam("corrId", required = false) corrId: String?,
    request: HttpServletRequest
  ): ResponseEntity<AuthResponseDto> {
    return runCatching {
      return ResponseEntity.ok(
        AuthResponseDto(
          token = authService.createTokenForWeb(request.remoteAddr)
        )
      )
    }.getOrElse {
      log.error("${it.message}")
      ResponseEntity.badRequest().build()
    }
  }
}
