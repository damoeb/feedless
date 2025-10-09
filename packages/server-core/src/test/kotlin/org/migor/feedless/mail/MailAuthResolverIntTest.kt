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
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.AuthViaMailInput
import org.migor.feedless.generated.types.Authentication
import org.migor.feedless.generated.types.ConfirmAuthCodeInput
import org.migor.feedless.generated.types.ConfirmCode
import org.migor.feedless.generated.types.Vertical
import org.migor.feedless.report.ReportService
import org.migor.feedless.repository.any2
import org.migor.feedless.session.PermissionService
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
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
@MockBeans(
  MockBean(ServerConfigResolver::class),
  MockBean(PermissionService::class),
  MockBean(OneTimePasswordDAO::class),
  MockBean(ReportService::class),
  MockBean(MailService::class),
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableMailConfiguration::class,
)
class MailAuthResolverIntTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @LocalServerPort
  private var port: Int = 0

  @MockBean
  lateinit var mailAuthenticationService: MailAuthenticationService

  @BeforeEach
  fun setUp() {
    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)
  }

  @Test
  fun authenticateUsingMail() = runTest {
    val confirmCode = ConfirmCode(
      length = 4,
      otpId = UUID.randomUUID().toString(),
    )
    Mockito.`when`(mailAuthenticationService.authenticateUsingMail(any2())).thenReturn(confirmCode)
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
    assertThat(auth[DgsConstants.CONFIRMCODE.OtpId] as String).isEqualTo(confirmCode.otpId)
  }

  @Test
  fun authConfirmCode() = runTest {
    val authentication = Authentication(
      corrId = UUID.randomUUID().toString(),
      token = UUID.randomUUID().toString()
    )
    Mockito.`when`(mailAuthenticationService.confirmAuthCode(any2(), any2())).thenReturn(authentication)
    val graphQLMutation = DgsClient.buildMutation {
      authConfirmCode(
        data = ConfirmAuthCodeInput(
          code = "someone@localhost",
          otpId = ""
        )
      ) {
        corrId
        token
      }
    }

    val response = monoGraphQLClient.reactiveExecuteQuery(graphQLMutation)
      .toFuture()
      .await()
      .extractValue<LinkedHashMap<String, Any>>("data.authConfirmCode")

    val auth = ObjectMapper().convertValue(response, Map::class.java)

    assertThat(auth[DgsConstants.AUTHENTICATION.CorrId] as String).isEqualTo(authentication.corrId)
    assertThat(auth[DgsConstants.AUTHENTICATION.Token] as String).isEqualTo(authentication.token)

  }
}
