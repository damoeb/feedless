package org.migor.feedless.session

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.ServletWebRequest
import org.migor.feedless.generated.types.Authentication as AuthenticationDto

@DgsComponent
@Profile("${AppProfiles.session} & ${AppLayer.api}")
class AuthAnonymousResolver {

  private val log = LoggerFactory.getLogger(AuthAnonymousResolver::class.simpleName)

  @Autowired
  private lateinit var tokenProvider: TokenProvider

  @Autowired
  private lateinit var cookieProvider: CookieProvider

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.AuthAnonymous)
  suspend fun authAnonymous(
    dfe: DataFetchingEnvironment,
  ): AuthenticationDto = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("authAnonymous")
    val jwt = tokenProvider.createJwtForAnonymous()
    addCookie(dfe, cookieProvider.createTokenCookie(jwt))
    AuthenticationDto(
      token = jwt.tokenValue,
      corrId = CryptUtil.newCorrId()
    )
  }

  private fun addCookie(dfe: DataFetchingEnvironment, cookie: Cookie) {
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
  }
}
