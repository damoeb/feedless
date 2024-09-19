package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsQueryExecutor
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.agent.AgentService
import org.migor.feedless.attachment.AttachmentDAO
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.types.ExtendContentOptions
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.HttpFetchInput
import org.migor.feedless.generated.types.HttpGetRequestInput
import org.migor.feedless.generated.types.PluginExecutionInput
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapeActionInput
import org.migor.feedless.generated.types.ScrapeFlowInput
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.generated.types.SourceInput
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ResourceUtils
import java.nio.file.Files

@SpringBootTest
@MockBeans(
  MockBean(ServerConfigResolver::class),
  MockBean(AgentService::class),
  MockBean(AttachmentDAO::class),
)
@ActiveProfiles(
  "test",
  AppProfiles.properties,
  AppLayer.api,
  AppLayer.service,
  AppProfiles.scrape,
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableWebSocketsConfiguration::class
)
class ScrapeQueryResolverTest {

  @Autowired
  lateinit var dgsQueryExecutor: DgsQueryExecutor

  @MockBean
  lateinit var httpServiceMock: HttpService

  val url = "http://www.foo.bar/something"

  @BeforeEach
  fun setUp() {
    mockSecurityContext()
  }

  @Test
  fun `scrape native feed`() = runTest {

    // given
    val url = "http://www.foo.bar/feed.xml"
    val feed = Files.readString(ResourceUtils.getFile("classpath:transform/medium-rss.in.xml").toPath())
    val httpResponse = HttpResponse("application/xml", url, 200, feed.toByteArray())
    `when`(httpServiceMock.httpGetCaching(anyString(), anyString(), anyInt(), any<Map<String, String>>()))
      .thenReturn(httpResponse)

    // when
    val scrapeResponse = scrapeFeed(PluginExecutionParamsInput())

    // then
    executeFeedAssertions(scrapeResponse)
  }

  @Test
  fun `scrape generic feed`() = runTest {
    // given
    val feed = Files.readString(ResourceUtils.getFile("classpath:raw-websites/06-jon-bo-posts.input.html").toPath())
    val httpResponse = HttpResponse("text/html", url, 200, feed.toByteArray())
    `when`(httpServiceMock.httpGetCaching(anyString(), anyString(), anyInt(), any<Map<String, String>>()))
      .thenReturn(httpResponse)
    val params = PluginExecutionParamsInput(
      org_feedless_feed = FeedParamsInput(
        generic = SelectorsInput(
          contextXPath = "//div[1]/div[1]/div[1]/div",
          linkXPath = "./h1[1]/a[1]",
          paginationXPath = "",
          dateXPath = "",
          dateIsStartOfEvent = false,
          extendContext = ExtendContentOptions.NONE
        )
      )
    )

    // when
    val scrapeResponse = scrapeFeed(params)

    // then
    executeFeedAssertions(scrapeResponse)
  }

  private fun scrapeFeed(params: PluginExecutionParamsInput): ScrapeResponse {
    val graphQLQuery = DgsClient.buildQuery {
      scrape(
        data = SourceInput(
          title = "",
          flow = ScrapeFlowInput(
            sequence = listOf(
              ScrapeActionInput(
                fetch = HttpFetchInput(
                  get = HttpGetRequestInput(
                    url = StringLiteralOrVariableInput(
                      literal = url
                    )
                  )
                )
              ),
              ScrapeActionInput(
                execute = PluginExecutionInput(
                  pluginId = FeedlessPlugins.org_feedless_feed.name,
                  params = params
                )
              )
            )
          )
        )
      ) {
        ok
        logs {
          time
          message
        }
        outputs {
          index
          response {
            extract {
              fragmentName
              items {
                publishedAt
                url
                createdAt
                id
              }
            }
          }
        }
      }
    }

    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
      graphQLQuery,
      "data.scrape",
      ScrapeResponse::class.java,
    )
  }

  private fun executeFeedAssertions(scrapeResponse: ScrapeResponse) {
    val items = scrapeResponse.outputs.find { it.response.extract != null }!!.response.extract!!.items!!
    assertThat(items.size).isGreaterThan(0)
  }

  private fun mockSecurityContext() {
    val authentication = TestingAuthenticationToken("", "", "WRITE")
    val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
    `when`(securityContext.authentication).thenReturn(authentication)
    SecurityContextHolder.setContext(securityContext)
  }
}
