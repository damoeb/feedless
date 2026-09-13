package org.migor.feedless.browserautomation

import com.google.gson.Gson
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.api.graphql.ServerConfigResolver
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
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.wheneverBlocking
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
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

    val received = LinkedBlockingQueue<String>()
    val handler = object : TextWebSocketHandler() {
      override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        received.add(message.payload)
      }
    }
    val headers = WebSocketHttpHeaders().apply { secWebSocketProtocol = listOf("graphql-transport-ws") }
    val session = StandardWebSocketClient()
      .execute(handler, headers, URI.create("ws://localhost:$port/subscriptions"))
      .get(10, TimeUnit.SECONDS)

    try {
      session.sendMessage(TextMessage("""{"type":"connection_init","payload":{}}"""))
      assertThat(received.poll(10, TimeUnit.SECONDS)).containsPattern(typeIs("connection_ack"))

      val subscribe = mapOf("id" to "1", "type" to "subscribe", "payload" to mapOf("query" to registerAgentQuery))
      session.sendMessage(TextMessage(Gson().toJson(subscribe)))
      assertThat(received.poll(10, TimeUnit.SECONDS))
        .containsPattern(typeIs("next"))
        .contains("test-token")
    } finally {
      session.close()
    }

    verifyBlocking(browserAutomationService) { registerAgent(argThat { secretKey.email == "agent@example.org" }) }
  }

  private fun typeIs(type: String) = "\"type\"\\s*:\\s*\"$type\""
}
