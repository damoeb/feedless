package org.migor.feedless.document

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.license.LicenseService
import org.migor.feedless.plan.ProductDataLoader
import org.migor.feedless.repository.RepositoryDAO
import org.migor.feedless.repository.any
import org.migor.feedless.user.UserService
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.socket.WebSocketHandler
import java.util.*


@ExtendWith(SpringExtension::class)
@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@MockBeans(
  value = [
    MockBean(UserService::class),
    MockBean(LicenseService::class),
    MockBean(DocumentService::class),
    MockBean(ProductDataLoader::class),
    MockBean(WebSocketHandler::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
)
@ActiveProfiles(profiles = ["test", AppProfiles.api])
class DocumentControllerTest {

  private lateinit var mockDocument: DocumentEntity
  private lateinit var endpointUrl: String
  private var documentUrl: String = "https://some-document-url.test"

  private var documentId: UUID = UUID.randomUUID()

  @Autowired
  lateinit var documentService: DocumentService

  @LocalServerPort
  var port = 0

  @BeforeEach
  fun setUp() {
    runBlocking {
      endpointUrl = "http://localhost:$port/article/$documentId"

      mockDocument = DocumentEntity()
      mockDocument.url = documentUrl
      mockDocument.contentText = "foo"
      mockDocument.repositoryId = UUID.randomUUID()
      mockDocument.status = ReleaseStatus.released

      Mockito.`when`(
        documentService.findById(
          any(UUID::class.java),
        )
      ).thenReturn(mockDocument)
    }
  }

  @Test
  fun `redirect with source param`() {
    val params = mapOf(
      "source" to "https://heise.de/some-feed.xml",
    )
    val restTemplate = TestRestTemplate()
    val response = restTemplate.exchange(endpointUrl, HttpMethod.GET, HttpEntity.EMPTY, String::class.java, params)
    assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
    assertThat(response.headers.getFirst(HttpHeaders.LOCATION)).isEqualTo(documentUrl)
  }

  @Test
  fun `redirect without source param`() {
    val restTemplate = TestRestTemplate()
    val response = restTemplate.exchange(endpointUrl, HttpMethod.GET, HttpEntity.EMPTY, String::class.java)
    assertThat(response.statusCode).isEqualTo(HttpStatus.FOUND)
    assertThat(response.headers.getFirst(HttpHeaders.LOCATION)).isEqualTo(documentUrl)
  }
}
