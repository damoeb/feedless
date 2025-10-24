package org.migor.feedless.document

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.analytics.AnalyticsService
import org.migor.feedless.any2
import org.migor.feedless.common.HttpService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.eq
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.SessionService
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserService
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*


@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@MockBeans(
  MockBean(DocumentResolver::class),
  MockBean(HttpService::class),
  MockBean(AuthService::class),
  MockBean(UserService::class),
  MockBean(SessionService::class),
  MockBean(PropertyService::class),
  MockBean(TokenProvider::class),
  MockBean(CookieProvider::class),
  MockBean(PermissionService::class),
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppProfiles.document,
  AppProfiles.session,
  AppLayer.security,
  AppLayer.service,
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableWebSocketsConfiguration::class
)
class DocumentControllerIntTest {

  private lateinit var document: DocumentEntity
  private var actualDocumentUrl: String = "https://some-document-url.test"

  @MockBean
  lateinit var documentService: DocumentService

  @MockBean
  lateinit var analyticsService: AnalyticsService

  @Autowired
  private lateinit var template: TestRestTemplate

  @BeforeEach
  fun setUp() = runTest {
    document = DocumentEntity()
    document.url = actualDocumentUrl
    document.text = "foo"
    document.repositoryId = UUID.randomUUID()
    document.status = ReleaseStatus.released
  }

  @Test
  fun `returns 404 if document does not exist`() = runTest {
    val response = template.getForEntity("/article/${document.id}", String::class.java)
    verify(documentService).findById(eq(DocumentId(document.id)))
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `redirect with source param`() = runTest {
    `when`(documentService.findById(any2())).thenReturn(document)

    val params = mapOf(
      "source" to "https://heise.de/some-feed.xml",
    )
    val response = template.getForEntity("/article/${document.id}", String::class.java, params)
    assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
    assertThat(response.headers.getFirst(HttpHeaders.LOCATION)).isEqualTo(actualDocumentUrl)
  }

  @Test
  fun `redirect without source param`() = runTest {
    `when`(documentService.findById(any2())).thenReturn(document)
    val response = template.getForEntity("/article/${document.id}", String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
    assertThat(response.headers.getFirst(HttpHeaders.LOCATION)).isEqualTo(actualDocumentUrl)
  }
}
