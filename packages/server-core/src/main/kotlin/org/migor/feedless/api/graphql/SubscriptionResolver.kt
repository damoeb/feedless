package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsSubscription
import com.netflix.graphql.dgs.InputArgument
import org.migor.feedless.AppProfiles
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
    log.info("authViaMail ${data.product}")
    val token = this.authService.decodeToken(data.token)
    return mailAuthenticationService.authenticateUsingMail(newCorrId(), data)
  }

  @DgsSubscription
  fun registerAgent(@InputArgument data: RegisterAgentInput): Publisher<AgentEvent> {
    log.info("registerAgent ${data.secretKey?.email}")
    return data.secretKey?.let { agentService.registerPrerenderAgent(data) }
      ?: throw IllegalArgumentException("expected secretKey, found none")
  }
}
