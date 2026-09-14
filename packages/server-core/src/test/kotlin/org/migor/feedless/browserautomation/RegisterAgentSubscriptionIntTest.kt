package org.migor.feedless.browserautomation

import com.google.gson.Gson
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.generated.types.AgentAuthentication
import org.migor.feedless.generated.types.AgentEvent
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.payment.PaymentUseCase
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.InboxService
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.session.actingGroupOf
import org.migor.feedless.user.User
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.isNull
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import reactor.core.publisher.Flux
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.UUID
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** Raw graphql-transport-ws, so the same test guards the legacy DGS handler and Spring for GraphQL. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
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
  AppProfiles.browserAutomation,
)
class RegisterAgentSubscriptionIntTest {

  @LocalServerPort
  private var port = 0

  @MockitoBean
  private lateinit var browserAutomationService: BrowserAutomationService

  @MockitoBean
  private lateinit var featureService: FeatureService

  @Autowired
  private lateinit var userUseCase: UserUseCase

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

  @Autowired
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

  private val sessionQuery = "query { session { isLoggedIn isAnonymous userId } }"

  private val registerAgentQuery = """
    subscription {
      registerAgent(data: {
        secretKey: {email: "agent@example.org", secretKey: "secret"},
        os: {arch: "x64", platform: "linux"},
        version: "1.0.0", name: "ws-test", connectionId: "ws-test-connection"
      }) { callbackId authentication { token } }
    }
  """.trimIndent()

  @Test
  fun `an agent subscribes to registerAgent over graphql-transport-ws`() {
    wheneverBlocking { browserAutomationService.registerAgent(any()) }
      .thenReturn(Flux.just(AgentEvent(callbackId = "none", corrId = "c1", authentication = AgentAuthentication(token = "test-token"))))

    assertThat(firstFrameAfterSubscribe())
      .containsPattern(typeIs("next"))
      .contains("test-token")

    verifyBlocking(browserAutomationService) { registerAgent(argThat { secretKey.email == "agent@example.org" }) }
  }

  @Test
  fun `a rejected agent receives the reason as a subscription error`() {
    wheneverBlocking { browserAutomationService.registerAgent(any()) }
      .thenReturn(Flux.error<AgentEvent>(IllegalAccessException("Key is expired")))

    assertThat(firstFrameAfterSubscribe())
      .containsPattern(typeIs("error"))
      .contains("Key is expired")
  }

  /** The TOKEN cookie rides along cross-site, so a WebSocket operation must never act as its user. */
  @Test
  fun `a WebSocket operation runs anonymously even with a user's TOKEN cookie`() {
    val cookie = "TOKEN=${sessionTokenOf(newUser())}"

    assertThat(firstFrameAfterSubscribe(sessionQuery, cookie))
      .containsPattern(typeIs("next"))
      .contains("\"isLoggedIn\":false")
      .contains("\"isAnonymous\":true")
  }

  @Test
  fun `the same TOKEN cookie authenticates the session query over HTTP`() {
    val user = newUser()
    val request = HttpRequest.newBuilder(URI("http://localhost:$port/graphql"))
      .header(HttpHeaders.COOKIE, "TOKEN=${sessionTokenOf(user)}")
      .header(HttpHeaders.CONTENT_TYPE, "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(Gson().toJson(mapOf("query" to sessionQuery))))
      .build()

    val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())

    assertThat(response.body())
      .contains("\"isLoggedIn\":true")
      .contains("\"userId\":\"${user.id.uuid}\"")
  }

  private fun newUser(): User = runBlocking {
    // Sign-up is feature-gated; the mock would answer null.
    wheneverBlocking { featureService.isDisabled(any(), isNull()) }.thenReturn(false)
    userUseCase.createUser("ws-anonymous+${UUID.randomUUID()}@feedless.test")
  }

  // The token a browser login puts into the TOKEN cookie (StatefulAuthService.authenticateUser).
  private fun sessionTokenOf(user: User): String =
    jwtTokenIssuer.createJwtForCapabilities(
      listOf(UserCapability(user.id), GroupCapability(userGroupAssignmentRepository.actingGroupOf(user.id))),
    ).tokenValue

  private fun firstFrameAfterSubscribe(query: String = registerAgentQuery, cookie: String? = null): String? {
    val received = LinkedBlockingQueue<String>()
    val handler = object : TextWebSocketHandler() {
      override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        received.add(message.payload)
      }
    }
    val headers = WebSocketHttpHeaders().apply {
      secWebSocketProtocol = listOf("graphql-transport-ws")
      cookie?.let { add(HttpHeaders.COOKIE, it) }
    }
    val session = StandardWebSocketClient()
      .execute(handler, headers, URI.create("ws://localhost:$port/subscriptions"))
      .get(10, TimeUnit.SECONDS)

    try {
      session.sendMessage(TextMessage("""{"type":"connection_init","payload":{}}"""))
      assertThat(received.poll(10, TimeUnit.SECONDS)).containsPattern(typeIs("connection_ack"))

      val subscribe = mapOf("id" to "1", "type" to "subscribe", "payload" to mapOf("query" to query))
      session.sendMessage(TextMessage(Gson().toJson(subscribe)))
      return received.poll(10, TimeUnit.SECONDS)
    } finally {
      session.close()
    }
  }

  private fun typeIs(type: String) = "\"type\"\\s*:\\s*\"$type\""
}
