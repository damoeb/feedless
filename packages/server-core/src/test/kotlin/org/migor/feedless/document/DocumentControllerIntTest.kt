package org.migor.feedless.document

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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
import org.migor.feedless.eq
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.session.AuthService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.PermissionService
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserService
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime


@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@MockitoBean(
    types = [
        DocumentResolver::class,
        HttpService::class,
        AuthService::class,
        UserService::class,
        SessionService::class,
        PropertyService::class,
        JwtTokenIssuer::class,
        CookieProvider::class,
        PermissionService::class,
        OAuth2AuthorizedClientService::class
    ]
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

    private lateinit var document: Document
    private var actualDocumentUrl: String = "https://some-document-url.test"

    @MockitoBean
    lateinit var documentService: DocumentService

    @MockitoBean
    lateinit var analyticsService: AnalyticsService

    @Autowired
    private lateinit var template: TestRestTemplate

    @BeforeEach
    fun setUp() = runTest {
        document = Document(
            url = actualDocumentUrl,
            text = "foo",
            repositoryId = RepositoryId(),
            status = ReleaseStatus.released,
            publishedAt = LocalDateTime.now(),
            contentHash = ""
        )
    }

    @Test
    fun `returns 404 if document does not exist`() = runTest {
        val response = template.getForEntity("/article/${document.id.uuid}", String::class.java)
        verify(documentService).findById(eq(document.id))
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    @Disabled
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
    @Disabled
    fun `redirect without source param`() = runTest {
        `when`(documentService.findById(any2())).thenReturn(document)
        val response = template.getForEntity("/article/${document.id}", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
        assertThat(response.headers.getFirst(HttpHeaders.LOCATION)).isEqualTo(actualDocumentUrl)
    }
}
