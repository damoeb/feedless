package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsQueryExecutor
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppProfiles
import org.migor.feedless.agent.AgentJob
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.common.HttpService
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.generated.types.RemoteNativeFeed
import org.migor.feedless.generated.types.ScrapeResponse
import org.migor.feedless.generated.types.SelectorsInput
import org.migor.feedless.license.LicenseService
import org.migor.feedless.session.SessionService
import org.migor.feedless.user.UserService
import org.migor.feedless.util.JsonUtil
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
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
    MockBean(SessionService::class),
    MockBean(LicenseService::class),
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
  fun `scrape native feed`() {
    // given
    val url = "http://www.foo.bar/feed.xml"
    val feed = Files.readString(ResourceUtils.getFile("classpath:transform/medium-rss.in.xml").toPath())
    val httpResponse = HttpResponse("application/xml", url, 200, feed.toByteArray())
    Mockito.`when`(httpServiceMock.httpGetCaching(anyString(), anyString(), anyInt(), any<Map<String, Any>>()))
      .thenReturn(httpResponse)

    // when
    val scrapeResponse = callScrape(PluginExecutionParamsInput.newBuilder().build())

    // then
    executeFeedAssertions(scrapeResponse)
  }

  @Test
  fun `scrape generic feed`() {
    // given
    val feed = Files.readString(ResourceUtils.getFile("classpath:raw-websites/06-jon-bo-posts.input.html").toPath())
    val httpResponse = HttpResponse("text/html", url, 200, feed.toByteArray())
    Mockito.`when`(httpServiceMock.httpGetCaching(anyString(), anyString(), anyInt(), any<Map<String, Any>>()))
      .thenReturn(httpResponse)
    val params = PluginExecutionParamsInput.newBuilder()
      .org_feedless_feed(
        SelectorsInput.newBuilder()
          .contextXPath("//div[1]/div[1]/div[1]/div")
          .linkXPath("./h1[1]/a[1]")
          .build()
      )
      .build()

    // when
    val scrapeResponse = callScrape(params)

    // then
    executeFeedAssertions(scrapeResponse)
  }

  private fun callScrape(params: PluginExecutionParamsInput): ScrapeResponse {
    return dgsQueryExecutor.executeAndExtractJsonPathAsObject(
      """
        query (${'$'}pluginId: ID!, ${'$'}params: PluginExecutionParamsInput!) {
          scrape(data: {
            page: {
              url: "$url"
            }
            emit: [
              {
                selectorBased: {
                  xpath: {value: "/"}
                  expose: {
                    transformers: [
                      {
                        pluginId: ${'$'}pluginId
                        params: ${'$'}params
                      }
                    ]
                  }
                }
              }
            ]
          }
          ) {
            elements {
              selector {
                fields {
                  name
                  value {
                    one {
                      mimeType
                      data
                    }
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
    val feedField = scrapeResponse.elements[0].selector.fields[0]
    Assertions.assertThat(feedField.name).isEqualTo(FeedlessPlugins.org_feedless_feed.name)
    Assertions.assertThat(feedField.value.one.mimeType).isEqualTo("application/json")
    val actualFeed = JsonUtil.gson.fromJson(feedField.value.one.data, RemoteNativeFeed::class.java)
    Assertions.assertThat(actualFeed).isNotNull
    Assertions.assertThat(actualFeed.items.size).isGreaterThan(0)
  }

  private fun mockSecurityContext() {
    val authentication = TestingAuthenticationToken("", "", "WRITE")
    val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
    Mockito.`when`(securityContext.authentication).thenReturn(authentication)
    SecurityContextHolder.setContext(securityContext)
  }
}
