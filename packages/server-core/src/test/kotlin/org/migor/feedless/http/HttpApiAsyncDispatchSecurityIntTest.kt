package org.migor.feedless.http

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.agent.AgentService
import org.migor.feedless.any
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.payment.PaymentUseCase
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.InboxService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userSecret.UserSecretRepository
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.UUID

/**
 * Every `/api/v1` handler is a `suspend fun`, so on a real servlet container Spring MVC completes it
 * through an ASYNC dispatch that runs the security filter chain a second time. MockMvc controller
 * tests never see that dispatch, so this drives the endpoints through a real port with a token
 * minted the way production mints API tokens: the caller must be authenticated at the URL level,
 * in `@PreAuthorize`, and in [RepositoryAccessGuard] (which reads the coroutine's RequestContext).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
// Collaborators outside the auth path this test drives, mocked like RepositoryUseCaseIntTest does.
@MockitoBean(
  types = [
    OAuth2AuthorizedClientService::class,
    ServerConfigResolver::class,
    UserSecretRepository::class,
    PaymentUseCase::class,
    ProductRepository::class,
    ProductUseCase::class,
    DocumentRepository::class,
    DocumentUseCase::class,
    InboxService::class,
    OrderRepository::class,
    AgentService::class,
    SourcePipelineService::class,
    PlanConstraintsService::class,
  ]
)
@ActiveProfiles(
  "test",
  "database",
  AppLayer.api,
  AppLayer.service,
  AppLayer.repository,
  AppLayer.security,
  AppProfiles.properties,
  AppProfiles.session,
  AppProfiles.user,
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.scrape,
)
class HttpApiAsyncDispatchSecurityIntTest {

  @LocalServerPort
  private var port = 0

  @Autowired
  private lateinit var userUseCase: UserUseCase

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  @MockitoBean
  private lateinit var featureService: FeatureService

  private val restTemplate = TestRestTemplate()

  private lateinit var caller: User
  private lateinit var callerRepository: Repository
  private lateinit var strangerRepository: Repository

  @BeforeEach
  fun setUp() = runBlocking {
    // Sign-up is feature-gated; the mock would answer null.
    `when`(featureService.isDisabled(any(FeatureName::class.java), eq(null))).thenReturn(false)
    caller = newUser()
    callerRepository = privateRepositoryOf(caller)
    strangerRepository = privateRepositoryOf(newUser())
  }

  @Test
  fun `GET user answers 200 with the caller for a valid API token`() {
    val response = get("/api/v1/user", apiToken(caller))

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.body).contains(caller.id.uuid.toString())
  }

  @Test
  fun `GET own repository sources answers 200 for a valid API token`() {
    val response = get("/api/v1/repositories/${callerRepository.id.uuid}/sources", apiToken(caller))

    assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(response.body).contains("\"items\"")
  }

  @Test
  fun `GET a stranger's private repository sources answers 404 for a valid API token`() {
    val response = get("/api/v1/repositories/${strangerRepository.id.uuid}/sources", apiToken(caller))

    // The guard's own answer — not a 404 that merely looks the same because the call never got there.
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(response.body).contains("repository ${strangerRepository.id.uuid} not found")
  }

  @Test
  fun `GET user without a token answers 401 with the version header`() {
    val response = get("/api/v1/user", token = null)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    assertThat(response.headers.getFirst("X-Feedless-Version")).isNotBlank()
  }

  @Test
  fun `GET user with an anonymous token answers 401 with the version header`() {
    val response = get("/api/v1/user", jwtTokenIssuer.createJwtForAnonymous().tokenValue)

    assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    assertThat(response.headers.getFirst("X-Feedless-Version")).isNotBlank()
  }

  private fun apiToken(user: User): String = jwtTokenIssuer.createJwtForApi(user).tokenValue

  private fun get(path: String, token: String?): ResponseEntity<String> {
    val headers = HttpHeaders()
    token?.let { headers.setBearerAuth(it) }
    return restTemplate.exchange(
      "http://localhost:$port$path",
      HttpMethod.GET,
      HttpEntity<Void>(headers),
      String::class.java,
    )
  }

  // Data is not reset between tests (shared Testcontainers Postgres), so every test seeds its own users.
  private suspend fun newUser(): User = userUseCase.createUser("async-dispatch+${UUID.randomUUID()}@feedless.test")

  private fun privateRepositoryOf(owner: User): Repository {
    val group = groupRepository.findAllByOwner(owner.id).single()
    return repositoryRepository.save(
      Repository(
        title = "private repository of ${owner.id.uuid}",
        visibility = EntityVisibility.isPrivate,
        ownerId = owner.id,
        groupId = group.id,
      ),
    )
  }
}
