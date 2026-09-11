package org.migor.feedless.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentController
import org.migor.feedless.feed.FeedService
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCasePort
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.SessionResolver
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * A misconfigured apiGatewayUrl must never reach the templated install.sh; a separate context, since the URL is fixed per context.
 * The value must still be a legal URI (HttpService parses it at startup), so it uses `?`, which the allow-list rejects too.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "app.apiGatewayUrl=https://feedless.example.org?q=1",
    "app.cli.installScriptLocation=classpath:cli/install-fixture.sh",
  ],
)
@MockitoBean(
  types = [
    ServerConfigResolver::class,
    UserRepository::class,
    UserSecretRepository::class,
    UserUseCase::class,
    SessionResolver::class,
    DocumentController::class,
    UserGuard::class,
    OneTimePasswordService::class,
    OAuth2AuthorizedClientService::class,
    FeedService::class,
    UserGroupAssignmentRepository::class,
    GroupRepository::class,
    GroupUseCasePort::class,
    AnalyticsService::class
  ]
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.session,
  AppProfiles.feed,
  "metrics"
)
@Import(DisableDatabaseConfiguration::class)
class CliInstallScriptControllerWithUnsafeGatewayUrlIntTest {

  @LocalServerPort
  var port = 0

  @Test
  fun `whenApiGatewayUrlIsUnsafe_ThenInternalServerErrorAndNoScriptBody`() {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.getForEntity("http://localhost:$port/cli/install.sh", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    assertThat(response.body).doesNotContain("q=1")
    assertThat(response.body).doesNotContain(CliInstallScriptController.feedctlBaseUrlPlaceholder)
    assertThat(response.body).doesNotContain("FEEDCTL_BASE_URL")
  }
}
