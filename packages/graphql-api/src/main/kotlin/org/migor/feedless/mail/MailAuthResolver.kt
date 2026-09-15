package org.migor.feedless.mail

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import com.netflix.graphql.dgs.context.DgsContext
import com.netflix.graphql.dgs.internal.DgsWebMvcRequestData
import graphql.schema.DataFetchingEnvironment
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.session.SessionTokenPort
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.migor.feedless.session.toServletCookie
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.ServletWebRequest

@DgsComponent
@Profile("${AppProfiles.mail} & ${AppLayer.api}")
class MailAuthResolver(
  private val mailAuthenticationService: MailAuthenticationService,
  private val sessionTokenPort: SessionTokenPort,
) {

  private val log = LoggerFactory.getLogger(MailAuthResolver::class.simpleName)

  @DgsMutation(field = DgsConstants.MUTATION.AuthenticateWithCodeViaMail)
  suspend fun authViaMail(@InputArgument(DgsConstants.MUTATION.AUTHENTICATEWITHCODEVIAMAIL_INPUT_ARGUMENT.Data) data: AuthViaMailInput): ConfirmCode =
    withContext(context = injectCapabilitiesFromSecurityContext()) {
      log.debug("authViaMail ${data.product}")
      mailAuthenticationService.authenticateUsingMail(data.email, data.allowCreate, data.osInfo)
        .let { ConfirmCode(length = it.length, otpId = it.otpId.uuid.toString()) }
    }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.AuthConfirmCode)
  suspend fun confirmAuthCode(
    @InputArgument(DgsConstants.MUTATION.AUTHCONFIRMCODE_INPUT_ARGUMENT.Data) data: ConfirmAuthCodeInput,
    dfe: DataFetchingEnvironment,
  ): Authentication = withContext(context = injectCapabilitiesFromSecurityContext()) {
    log.debug("confirmAuthCode")
    // Resolved before the code is consumed, as when the service took the response.
    val response = resolveHttpResponse(dfe)
    val token = mailAuthenticationService.confirmAuthCode(OneTimePasswordId(data.otpId), data.code)
    response.addCookie(toServletCookie(sessionTokenPort.toCookie(token)))
    Authentication(
      corrId = "",
      token = token.token
    )
  }

  private fun resolveHttpResponse(dfe: DataFetchingEnvironment): HttpServletResponse {
    return ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!
  }

}
