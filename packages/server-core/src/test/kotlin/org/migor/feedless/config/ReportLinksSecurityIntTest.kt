package org.migor.feedless.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentController
import org.migor.feedless.feed.FeedService
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.group.GroupUseCase
import org.migor.feedless.report.ReportGuard
import org.migor.feedless.report.ReportUseCase
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.secrets.OneTimePasswordService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.SessionResolver
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.template.TemplateService
import org.migor.feedless.user.UserGuard
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.migor.feedless.userSecret.UserSecretRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

/** Recipients have no account, so every link in a report mail must pass the real filter chain anonymously. */
@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = ["app.actuatorPassword=$actuatorPassword"],
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
    SourceRepository::class,
    RepositoryGuard::class,
    UserGroupAssignmentRepository::class,
    GroupRepository::class,
    GroupUseCase::class,
    AnalyticsService::class,
    ReportUseCase::class,
    ReportGuard::class,
    TemplateService::class,
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
  AppProfiles.report,
  "metrics"
)
@Import(DisableDatabaseConfiguration::class)
class ReportLinksSecurityIntTest {

  @LocalServerPort
  var port = 0

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  private fun get(path: String) = TestRestTemplate().getForEntity("http://localhost:$port$path", String::class.java)

  @Test
  fun `the cancel link works without login`() {
    val id = UUID.randomUUID().toString()
    val token = jwtTokenIssuer.createJwtForReport(id, 1).tokenValue

    assertThat(get("${ApiUrls.reportDelete}/$id?token=$token").statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `the confirm link works without login`() {
    val id = UUID.randomUUID().toString()
    val token = jwtTokenIssuer.createJwtForReport(id, 1).tokenValue

    assertThat(get("${ApiUrls.reportConfirm}/$id?token=$token").statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `the abuse link works without login`() {
    val id = UUID.randomUUID().toString()
    val token = jwtTokenIssuer.createJwtForRecipient(id, 1).tokenValue

    assertThat(get("${ApiUrls.reportAbuse}/$id?token=$token").statusCode).isEqualTo(HttpStatus.OK)
  }

  /** Guards against a filter chain that turned permissive everywhere. */
  @Test
  fun `a protected path still needs login`() {
    assertThat(get("/api/v1/user").statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
  }
}
