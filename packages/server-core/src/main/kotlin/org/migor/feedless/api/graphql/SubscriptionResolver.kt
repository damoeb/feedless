package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.api.auth.AuthService
import org.migor.feedless.api.auth.MailAuthenticationService
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.AuthenticationEvent
import org.migor.feedless.generated.types.RegisterAgentInput
import org.migor.feedless.service.AgentService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile


@DgsComponent
@Profile(AppProfiles.database)
class SubscriptionResolver {

  private val log = LoggerFactory.getLogger(SubscriptionResolver::class.simpleName)

  @Autowired
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  lateinit var agentService: AgentService

  @Autowired
  lateinit var authService: AuthService

  @DgsSubscription
  fun authViaMail(@InputArgument data: AuthViaMailInput): Publisher<AuthenticationEvent> {
    val corrId = newCorrId()
    log.info("[$corrId] authViaMail ${data.product}")
    this.authService.decodeToken(data.token)
    return mailAuthenticationService.authenticateUsingMail(corrId, data)
  }

  @DgsSubscription
  fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEvent> {
    val corrId = newCorrId()
    log.info("[$corrId] registerAgent ${data.secretKey?.email}")
    return data.secretKey?.let { agentService.registerPrerenderAgent(corrId, data) }
      ?: throw PermissionDeniedException("expected secretKey, found none ($corrId)")
  }
}
