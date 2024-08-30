package org.migor.feedless.api.graphql

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.netflix.graphql.dgs.DgsQueryExecutor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.AgentJob
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.document.DocumentService
import org.migor.feedless.generated.types.ExtendContentOptions
import org.migor.feedless.generated.types.FeedParamsInput
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.license.LicenseService
import org.migor.feedless.plan.ProductService
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserService
import org.migor.feedless.util.JsonUtil
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ResourceUtils
import java.nio.file.Files

@SpringBootTest
@ActiveProfiles(profiles = ["test", AppProfiles.api, AppProfiles.scrape])
@MockBeans(
  value = [
    MockBean(AgentService::class),
    MockBean(AgentJob::class),
    MockBean(UserService::class),
    MockBean(DocumentService::class),
    MockBean(SessionService::class),
    MockBean(LicenseService::class),
    MockBean(ProductService::class),
    MockBean(KotlinJdslJpqlExecutor::class),
  ]
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
  @Disabled
  fun `scrape native feed`() {
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
  @Disabled
  fun `scrape generic feed`() {
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
    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
      """
        query (${'$'}pluginId: ID!, ${'$'}params: PluginExecutionParamsInput!) {
        scrape(data: {
          title: ""
          flow: {
            sequence: [
              {
                fetch: {get: {url: {literal: "$url"}}}
              }
              {
                execute: {
                  pluginId: ${'$'}pluginId
                  params: ${'$'}params
                }
              }
            ]
          }
        }
        ) {
          failed
          logs
          outputs {
            index
            response {
              extract {
                items {
                  publishedAt
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
        """.trimIndent(),
      "data.scrape",
      mapOf(
        "pluginId" to FeedlessPlugins.org_feedless_feed.name,
        "params" to JsonUtil.gson.fromJson(JsonUtil.gson.toJson(params), Map::class.java)
      ),
      ScrapeResponse::class.java,
    )
  }

  private fun executeFeedAssertions(scrapeResponse: ScrapeResponse) {
//    val actualFeed = scrapeResponse.outputs.find { it.response.extract?.pluginId == FeedlessPlugins.org_feedless_feed.name }!!.response.execute!!.data.org_feedless_feed
//    Assertions.assertThat(actualFeed!!.items.size).isGreaterThan(0)
  }

  private fun mockSecurityContext() {
    val authentication = TestingAuthenticationToken("", "", "WRITE")
    val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
    `when`(securityContext.authentication).thenReturn(authentication)
    SecurityContextHolder.setContext(securityContext)
  }
}
