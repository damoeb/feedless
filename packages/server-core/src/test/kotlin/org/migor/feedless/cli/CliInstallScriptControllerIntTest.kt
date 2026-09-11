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
 * feedctl builds are cross-compiled and baked into the image separately
 * (the Go stage of packages/server-core/Dockerfile); this test only covers what
 * CliInstallScriptController is responsible for: templating install.sh with
 * this instance's own public URL. See SecurityConfigIntTest for the CLI
 * static location's public whitelisting.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "app.apiGatewayUrl=https://my-instance.example.com",
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
class CliInstallScriptControllerIntTest {

  lateinit var baseEndpoint: String

  @LocalServerPort
  var port = 0

  @Test
  fun `whenInstallScriptFixtureIsAbsent_ThenNotFound`() {
    val restTemplate = TestRestTemplate()
    baseEndpoint = "http://localhost:$port"

    // No feedctl build on disk for the default `app.cli.installScriptLocation`
    // (only the image's Go stage produces one) -- must not fail bootRun (see
    // packages/cli/README.md), just 404.
    val response = restTemplate.getForEntity("$baseEndpoint/cli/install.sh", String::class.java)

    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }
}
