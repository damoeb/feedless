package org.migor.feedless.mail

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.common.PropertyService
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.session.AuthService
import org.migor.feedless.util.CryptUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.context.request.ServletWebRequest

@DgsComponent
@Profile("${AppProfiles.database} & ${AppProfiles.mail}")
class MailAuthResolver {

  private val log = LoggerFactory.getLogger(MailAuthResolver::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var authService: AuthService

  @DgsSubscription
  fun authViaMail(@InputArgument data: AuthViaMailInput): Publisher<AuthenticationEvent> {
    val corrId = CryptUtil.newCorrId()
    log.info("[$corrId] authViaMail ${data.product}")
    this.authService.decodeToken(data.token)
    return mailAuthenticationService.authenticateUsingMail(corrId, data)
  }

  @Throttled
  @DgsMutation
  suspend fun authConfirmCode(
    @InputArgument data: ConfirmAuthCodeInput,
    dfe: DataFetchingEnvironment,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    log.info("[$corrId] authConfirmCode")
    mailAuthenticationService.confirmAuthCode(corrId, data, resolveHttpResponse(dfe))
    true
  }

  private fun resolveHttpResponse(dfe: DataFetchingEnvironment): HttpServletResponse {
    return ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!
  }

}
