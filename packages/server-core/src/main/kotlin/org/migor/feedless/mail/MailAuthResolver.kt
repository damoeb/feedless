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
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.session.createRequestContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.web.context.request.ServletWebRequest

@DgsComponent
@Profile("${AppProfiles.mail} & ${AppLayer.api}")
class MailAuthResolver(
  private val mailAuthenticationService: MailAuthenticationService,
) {

  private val log = LoggerFactory.getLogger(MailAuthResolver::class.simpleName)

  @DgsMutation(field = DgsConstants.MUTATION.AuthenticateWithCodeViaMail)
  suspend fun authViaMail(@InputArgument(DgsConstants.MUTATION.AUTHENTICATEWITHCODEVIAMAIL_INPUT_ARGUMENT.Data) data: AuthViaMailInput): ConfirmCode =
    withContext(context = createRequestContext()) {
      log.debug("authViaMail ${data.product}")
      mailAuthenticationService.authenticateUsingMail(data)
    }

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.AuthConfirmCode)
  suspend fun confirmAuthCode(
    @InputArgument(DgsConstants.MUTATION.AUTHCONFIRMCODE_INPUT_ARGUMENT.Data) data: ConfirmAuthCodeInput,
    dfe: DataFetchingEnvironment,
  ): Authentication = withContext(context = createRequestContext()) {
    log.debug("confirmAuthCode")
    mailAuthenticationService.confirmAuthCode(data, resolveHttpResponse(dfe))
  }

  private fun resolveHttpResponse(dfe: DataFetchingEnvironment): HttpServletResponse {
    return ((DgsContext.getRequestData(dfe)!! as DgsWebMvcRequestData).webRequest!! as ServletWebRequest).response!!
  }

}
