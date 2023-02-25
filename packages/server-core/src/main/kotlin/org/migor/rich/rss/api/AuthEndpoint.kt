package org.migor.rich.rss.api

import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class AuthEndpoint {

  private val log = LoggerFactory.getLogger(AuthEndpoint::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var propertyService: PropertyService

//  @Throttled
//  @Timed
//  @GetMapping("/api/auth")
//  fun auth(
//    @Header(ApiParams.corrId, required = false) corrId: String?,
//    request: HttpServletRequest,
//    response: HttpServletResponse
//  ): ResponseEntity<AuthResponseDto> {
//    return runCatching {
//      authService.issueWebToken(request, response)
////      response.addHeader("X-CORR-ID", newCorrId())
//      return ResponseEntity.ok(authService.authForWeb())
//    }.getOrElse {
//      log.error("${it.message}")
//      ResponseEntity.badRequest().build()
//    }
//  }

  @GetMapping(ApiUrls.magicMail)
  fun magicMail(
    @RequestParam(ApiParams.nonce) nonce: String,
  ) {
    authService.authorizeViaMail(nonce)
  }
}
