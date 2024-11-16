package org.migor.feedless.api.http

import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Tag
import org.migor.feedless.AppProfiles
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.util.ResourceUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files
import java.time.LocalDateTime

@RestController
@Profile(AppProfiles.DEV_ONLY)
@Tag("stable")
class TestingEndpoint {

  private val log = LoggerFactory.getLogger(TestingEndpoint::class.simpleName)

  @Autowired
  private lateinit var tokenProvider: TokenProvider

  @Autowired
  private lateinit var userDAO: UserDAO

  @Autowired
  private lateinit var cookieProvider: CookieProvider


  @GetMapping("testing/create-token")
  suspend fun createTestToken(response: HttpServletResponse) = coroutineScope {
    val user = withContext(Dispatchers.IO) {
      userDAO.findFirstByAdminIsTrue() ?: throw IllegalArgumentException("root user not found")
    }
    if (!user.hasAcceptedTerms) {
      user.hasAcceptedTerms = true
      user.acceptedTermsAt = LocalDateTime.now()
      withContext(Dispatchers.IO) {
        userDAO.save(user)
      }
    }
    response.addCookie(cookieProvider.createTokenCookie(tokenProvider.createJwtForUser(user)))
  }

  @GetMapping("/testing/file/{file}")
  suspend fun staticFile(@PathVariable file: String): ResponseEntity<String> = coroutineScope {
    withContext(Dispatchers.IO) {
      ResponseEntity.ok(Files.readString(ResourceUtils.getFile("classpath:test-data/$file").toPath()))
    }
  }
}
