package org.migor.rich.rss.api

import jakarta.servlet.http.HttpServletResponse
import org.migor.rich.rss.service.MailAuthenticationService
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
  lateinit var mailAuthenticationService: MailAuthenticationService

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
    @RequestParam("k") nonce: String,
    @RequestParam("i") otpId: String,
    @RequestParam("c") corrId: String,
  ): String {
    return mailAuthenticationService.authorizeViaMail(corrId, otpId, nonce)
  }

  @GetMapping(ApiUrls.login)
  fun login(response: HttpServletResponse) {
    response.sendRedirect("/oauth2/authorization/google")
  }
}
