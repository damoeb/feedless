package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.service.PropertyService
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.CryptUtil.handleCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest
import org.migor.feedless.generated.types.Authentication as AuthenticationDto

@DgsComponent
class AuthAnonymousResolver {

  private val log = LoggerFactory.getLogger(AuthAnonymousResolver::class.simpleName)

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var propertyService: PropertyService

  @Throttled
  @DgsMutation
  suspend fun authAnonymous(@RequestHeader(ApiParams.corrId, required = false) cid: String,
                            dfe: DataFetchingEnvironment,
  ): AuthenticationDto = coroutineScope {
    val corrId = handleCorrId(cid)
    log.info("[$corrId] authAnonymous")
    val jwt = tokenProvider.createJwtForAnonymous()
    addCookie(dfe, cookieProvider.createTokenCookie(corrId, jwt))
    AuthenticationDto.newBuilder()
      .token(jwt.tokenValue)
      .corrId(CryptUtil.newCorrId())
      .build()
  }

  private fun addCookie(dfe: DataFetchingEnvironment, cookie: Cookie) {
    ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!.addCookie(
      cookie
    )
  }
}