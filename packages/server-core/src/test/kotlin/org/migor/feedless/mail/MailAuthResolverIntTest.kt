package org.migor.feedless.mail

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableMailConfiguration
import org.migor.feedless.any2
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.report.ReportUseCase
import org.migor.feedless.session.TokenIssuer
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserId
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.reactive.function.client.WebClient
import java.util.*

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "app.apiGatewayUrl=https://localhost",
    "app.actuatorPassword=s3cr3t",
  ],
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.mail,
  AppProfiles.session,
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    UserGuard::class,
    OneTimePasswordRepository::class,
    ReportUseCase::class,
    MailService::class,
    UserRepository::class,
    UserGroupAssignmentRepository::class,
    GroupRepository::class,
    OAuth2AuthorizedClientService::class
  ]
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableMailConfiguration::class,
)
class MailAuthResolverIntTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @LocalServerPort
  private var port: Int = 0

  @MockitoBean
  lateinit var mailAuthenticationService: MailAuthenticationService

  @Autowired
  lateinit var tokenIssuer: TokenIssuer

  @BeforeEach
  fun setUp() {
    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)
  }

  @Test
  fun authenticateUsingMail() = runTest {
    val confirmCode = OtpChallenge(
      length = 4,
      otpId = OneTimePasswordId(),
    )
    Mockito.`when`(mailAuthenticationService.authenticateUsingMail(any2(), anyBoolean(), any2())).thenReturn(confirmCode)
    val graphQLMutation = DgsClient.buildMutation {
      authenticateWithCodeViaMail(
        data = AuthViaMailInput(
          email = "someone@localhost",
          product = Vertical.feedless,
          osInfo = "Linux",
          allowCreate = false
        )
      ) {
        length
        otpId
      }
    }

    val response = monoGraphQLClient.reactiveExecuteQuery(graphQLMutation)
      .toFuture()
      .await()
      .extractValue<LinkedHashMap<String, Any>>("data.authenticateWithCodeViaMail")

    val auth = ObjectMapper().convertValue(response, Map::class.java)

    assertThat(auth[DgsConstants.CONFIRMCODE.Length] as Int).isEqualTo(confirmCode.length)
    assertThat(auth[DgsConstants.CONFIRMCODE.OtpId] as String).isEqualTo(confirmCode.otpId.uuid.toString())
  }

  @Test
  fun confirmAuthCode() = runTest {
    // toCookie decodes the token, so it must be a real one
    val authentication = tokenIssuer.issueTokenForCapabilities(listOf(UserCapability(UserId())))
    Mockito.`when`(mailAuthenticationService.confirmAuthCode(any2(), any2())).thenReturn(authentication)
    val graphQLMutation = DgsClient.buildMutation {
      authConfirmCode(
        data = ConfirmAuthCodeInput(
          code = "someone@localhost",
          otpId = UUID.randomUUID().toString()
        )
      ) {
        corrId
        token
      }
    }

    val graphQLResponse = monoGraphQLClient.reactiveExecuteQuery(graphQLMutation)
      .toFuture()
      .await()
    val response = graphQLResponse.extractValue<LinkedHashMap<String, Any>>("data.authConfirmCode")

    val auth = ObjectMapper().convertValue(response, Map::class.java)

    assertThat(auth[DgsConstants.AUTHENTICATION.CorrId] as String).isEqualTo("")
    assertThat(auth[DgsConstants.AUTHENTICATION.Token] as String).isEqualTo(authentication.token)
    val setCookies = graphQLResponse.headers.entries
      .filter { it.key.equals("Set-Cookie", ignoreCase = true) }
      .flatMap { it.value }
    assertThat(setCookies).anyMatch { it.startsWith("TOKEN=${authentication.token}") && it.contains("HttpOnly") }
  }
}
