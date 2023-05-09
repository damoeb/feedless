package org.migor.feedless.api.http

import jakarta.servlet.http.HttpServletResponse
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.auth.MailAuthenticationService
import org.migor.feedless.service.PropertyService
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
