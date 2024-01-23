package org.migor.feedless.api.http

import jakarta.servlet.http.HttpServletResponse
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.sql.Timestamp
import java.util.*

@RestController
@Profile(AppProfiles.testing)
class TestingAuthEndpoint {

  private val log = LoggerFactory.getLogger(TestingAuthEndpoint::class.simpleName)

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var cookieProvider: CookieProvider


  @GetMapping("testing/create-token")
  fun createTestToken(response: HttpServletResponse) {
    val user = userDAO.findRootUser() ?: throw IllegalArgumentException("root user not found")
    if (!user.hasAcceptedTerms) {
      user.hasAcceptedTerms = true
      user.acceptedTermsAt = Timestamp.from(Date().toInstant())
      userDAO.save(user)
    }
    response.addCookie(cookieProvider.createTokenCookie(tokenProvider.createJwtForUser(user)))
  }
}
