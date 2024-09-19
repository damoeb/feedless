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
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.session.AuthService
import org.migor.feedless.util.CryptUtil
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.ServletWebRequest

@DgsComponent
@Profile("${AppProfiles.mail} & ${AppLayer.api}")
class MailAuthResolver {

  private val log = LoggerFactory.getLogger(MailAuthResolver::class.simpleName)

  @Autowired
  private lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  private lateinit var authService: AuthService

  @DgsSubscription
  suspend fun authViaMail(@InputArgument data: AuthViaMailInput): Publisher<AuthenticationEvent> = coroutineScope {
    val corrId = CryptUtil.newCorrId()
    log.debug("[$corrId] authViaMail ${data.product}")
    authService.decodeToken(data.token)
    mailAuthenticationService.authenticateUsingMail(data)
  }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.AuthConfirmCode)
  suspend fun authConfirmCode(
    @InputArgument data: ConfirmAuthCodeInput,
    dfe: DataFetchingEnvironment,
  ): Boolean = coroutineScope {
    log.debug("authConfirmCode")
    mailAuthenticationService.confirmAuthCode(data, resolveHttpResponse(dfe))
    true
  }

  private fun resolveHttpResponse(dfe: DataFetchingEnvironment): HttpServletResponse {
    return ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!
  }

}
