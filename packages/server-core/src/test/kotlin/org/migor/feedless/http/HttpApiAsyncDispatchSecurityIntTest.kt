package org.migor.feedless.http

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
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
import org.migor.feedless.any2
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.capability.RequestContext
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
import org.migor.feedless.secrets.UserSecretUseCase
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.SessionTokenPort
import org.migor.feedless.session.actingGroupOf
import org.migor.feedless.source.Source
import org.migor.feedless.source.SourceRepository
import org.migor.feedless.user.User
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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
  AppProfiles.secrets,
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

  @Autowired
  private lateinit var sourceRepository: SourceRepository

  @Autowired
  private lateinit var userSecretUseCase: UserSecretUseCase

  @Autowired
  private lateinit var sessionTokenPort: SessionTokenPort

  @Autowired
  private lateinit var planConstraintsService: PlanConstraintsService

  @Autowired
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

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

    // The guard's own answer, rendered by HttpApiExceptionHandler as an ApiError — not a 404 that merely
    // looks the same because the call never got there, or because another advice answered it.
    assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(response.body)
      .contains("\"code\":\"NOT_FOUND\"")
      .contains("\"message\":\"repository ${strangerRepository.id.uuid} not found\"")
  }

  /**
   * A non-NotFound domain error must keep its own status and ApiError code on the real container:
   * feedctl branches on both for every error path.
   */
  @Test
  fun `PATCH a source with a stale If-Match answers 412 PRECONDITION_FAILED`() {
    val response = send("PATCH", ownSourcePath(), "{\"title\":\"renamed\"}", HttpHeaders.IF_MATCH to "\"stale\"")

    assertThat(response.statusCode()).isEqualTo(HttpStatus.PRECONDITION_FAILED.value())
    assertThat(response.body()).contains("\"code\":\"PRECONDITION_FAILED\"")
  }

  /**
   * A write lands in the group the token acts in. Tokens minted by `createUserSecret` (API JWT) and
   * `authUser` (session JWT) used to carry no group, so creating a repository crashed with a 500.
   */
  @Test
  fun `POST repositories answers 201 in the caller's owner group for an API token from createUserSecret`() {
    val token = runBlocking {
      withContext(RequestContext(userId = caller.id)) { userSecretUseCase.createUserSecret().value }
    }

    assertCreatesRepositoryInOwnerGroup(token)
  }

  @Test
  fun `POST repositories answers 201 in the caller's owner group for a session token from authUser`() {
    val secretKey = runBlocking {
      withContext(RequestContext(userId = caller.id)) { userSecretUseCase.createUserSecret().value }
    }
    val token = runBlocking { sessionTokenPort.authenticateUser(caller.email, secretKey).token }

    assertCreatesRepositoryInOwnerGroup(token)
  }

  private fun assertCreatesRepositoryInOwnerGroup(token: String) {
    whenever(planConstraintsService.coerceVisibility(any2(), anyOrNull())).thenReturn(EntityVisibility.isPrivate)
    val title = "created by ${caller.id.uuid}"

    val response = send(
      "POST",
      "/api/v1/repositories",
      "{\"product\":\"feedless\",\"sources\":[],\"title\":\"$title\",\"description\":\"\"}",
      token = token,
    )

    assertThat(response.statusCode()).describedAs(response.body()).isEqualTo(HttpStatus.CREATED.value())
    val created = repositoryRepository.findByTitleAndOwnerId(title, caller.id)!!
    assertThat(created.groupId).isEqualTo(groupRepository.findAllByOwner(caller.id).single().id)
  }

  // Spring MVC's own exceptions on /api/v1 must keep their 4xx and still answer an ApiError.

  @Test
  fun `PATCH a source with a malformed JSON body answers 400 BAD_REQUEST`() {
    val response = send("PATCH", ownSourcePath(), "{\"title\":")

    assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    assertThat(response.body()).contains("\"code\":\"BAD_REQUEST\"")
  }

  @Test
  fun `GET sources of a non-UUID repository id answers 400 BAD_REQUEST`() {
    val response = send("GET", "/api/v1/repositories/not-a-uuid/sources")

    assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value())
    assertThat(response.body()).contains("\"code\":\"BAD_REQUEST\"")
  }

  @Test
  fun `PUT on a PATCH-only source path answers 405 METHOD_NOT_ALLOWED`() {
    val response = send("PUT", ownSourcePath(), "{\"title\":\"renamed\"}")

    assertThat(response.statusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED.value())
    assertThat(response.body()).contains("\"code\":\"METHOD_NOT_ALLOWED\"")
  }

  @Test
  fun `GET an unknown api v1 path answers 404 NOT_FOUND`() {
    val response = send("GET", "/api/v1/no-such-endpoint")

    assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value())
    assertThat(response.body()).contains("\"code\":\"NOT_FOUND\"")
  }

  private fun ownSourcePath(): String {
    val source = sourceRepository.save(Source(title = "source of ${caller.id.uuid}", repositoryId = callerRepository.id))
    return "/api/v1/repositories/${callerRepository.id.uuid}/sources/${source.id.uuid}"
  }

  /** JDK client: unlike TestRestTemplate's default request factory it sends PATCH, and never retries or rewrites. */
  private fun send(
    method: String,
    path: String,
    body: String? = null,
    vararg headers: Pair<String, String>,
    token: String = apiToken(caller),
  ): HttpResponse<String> {
    val builder = HttpRequest.newBuilder(URI("http://localhost:$port$path"))
      .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
      .header(HttpHeaders.ACCEPT, "application/json")
    headers.forEach { (name, value) -> builder.header(name, value) }
    if (body != null) {
      builder.header(HttpHeaders.CONTENT_TYPE, "application/json")
    }
    builder.method(method, body?.let { HttpRequest.BodyPublishers.ofString(it) } ?: HttpRequest.BodyPublishers.noBody())
    return HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString())
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

  private fun apiToken(user: User): String =
    jwtTokenIssuer.createJwtForApi(user, userGroupAssignmentRepository.actingGroupOf(user.id)).tokenValue

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
