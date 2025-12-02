package org.migor.feedless.transform

import com.google.gson.GsonBuilder
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableWebSocketsConfiguration
import org.migor.feedless.agent.AgentService
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.attachment.AttachmentDAO
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.scrape.GenericFeedParserOptions
import org.migor.feedless.scrape.WebExtractService
import org.migor.feedless.scrape.WebToFeedTransformer
import org.migor.feedless.scrape.WebToFeedTransformer.Companion.toAbsoluteUrl
import org.migor.feedless.scrape.WebToTextTransformer
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.source.SourceUseCase
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.util.ResourceUtils
import us.codecraft.xsoup.Xsoup
import java.net.URI
import java.nio.file.Files
import java.util.*


@SpringBootTest
@ActiveProfiles(
  "test",
  AppProfiles.scrape,
  AppLayer.service,
  AppProfiles.properties,
)
@MockitoBean(
  types = [
    AgentService::class,
    AttachmentDAO::class,
    SourceUseCase::class,
    StatelessAuthService::class,
  ]
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableWebSocketsConfiguration::class,
)
internal class WebToFeedTransformerTest {

  private lateinit var parser: WebToFeedTransformer

  @Autowired
  lateinit var webExtractService: WebExtractService

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    Mockito.`when`(propertyService.locale).thenReturn(Locale.forLanguageTag("en"))
    Mockito.`when`(propertyService.apiGatewayUrl).thenReturn("http://localhost:8080")

    parser = WebToFeedTransformer(propertyService, WebToTextTransformer(), webExtractService)
  }

  @Test
  fun generalizeXPathsSimple() = runTest {
    val xpaths = listOf(
      "//body/table[1]/tbody[1]/tr[1]/td[3]/table[1]/tbody[1]/tr[1]/td[1]/font[1]/a[1]",
      "//body/table[1]/tbody[1]/tr[1]/td[3]/table[1]/tbody[1]/tr[1]/td[1]/font[1]/a[2]",
      "//body/table[1]/tbody[1]/tr[1]/td[3]/table[1]/tbody[1]/tr[1]/td[1]/font[1]/a[3]",
      "//body/table[1]/tbody[1]/tr[1]/td[3]/table[2]/tbody[1]/tr[2]/td[1]/font[1]/a[1]",
      "//body/table[1]/tbody[1]/tr[1]/td[3]/table[2]/tbody[1]/tr[4]/td[1]/font[1]/a[1]"
    )

    assertEquals(
      "body/table[1]/tbody[1]/tr[1]/td[3]/table/tbody[1]/tr/td[1]/font[1]/a",
      parser.__generalizeXPaths(xpaths)
    )
  }

  @Test
  fun generalizeXPathsComplex() = runTest {
    val xpaths = listOf(
      "//div[@id='democracy']/ul[1]/li[2]",
      "//div[@id='democracy']/ul[1]/li[5]",
      "//div[@id='democracy']/ul[1]/li[9]",
      "//div[@id='economy']/ul[1]/li[1]",
      "//div[@id='economy']/ul[1]/li[8]",
      "//div[@id='health']/ul[1]/li[10]"
    )
    assertEquals(
      "div[contains(id, 'democracy') or contains(id, 'economy') or contains(id, 'health')]/ul[1]/li",
      parser.__generalizeXPaths(xpaths)
    )
  }

  @Test
  fun testRelativeXPath() = runTest {
    val document = Jsoup.parse(
      """
      <div>
        <ul>
          <li></li>
          <li><a><em></em></a></li>
          <li></li>
        </ul>
      </div>
    """.trimIndent()
    )

    val root = document.select("div").first()!!
    val child = document.select("em").first()!!
    val relativeXPath = parser.getRelativeXPath(child, root)
//    assertThat(relativeXPath).isEqualTo("")
    assertThat(Xsoup.compile(relativeXPath).evaluate(root).elements.first()).isEqualTo(child)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "https://webiphany.com/, 10-webiphany-com",
      "https://blog.spencermounta.in, 01-spencermounta-in",
      "https://spotify.com, 02-spotify-com",
      "https://telepolis.de, 03-telepolis-de",
//      "https://arzg.github.io/lang, 04-arzg-github-io-lang",
      "https://www.brandonsmith.ninja, 05-www-brandonsmith-ninja",
//      "https://jon.bo/posts, 06-jon-bo-posts",
//      "https://paulgraham.com, 00-paulgraham-com-articles",
      "https://www.fool.com/author/20415, 09-fool-com",
      "https://www.audacityteam.org/posts/, 11-audacityteam-org",
      "https://cloud.google.com/blog, 13-google-cloud-blog",
      "https://demo.linkace.org/guest/links, 14-linkace-org",
//      "https://abilene.craigslist.org, 07-craigslist",
//      "https://arxiv.org/list/math.GN/recent, 08-arxiv-org",
      "https://sph.ethz.ch/news, 12-sph-ethz-ch", // todo expand context
    ]
  )
  fun testSiteIsSupported(url: String, id: String) = runTest {
    testSupport(url, id)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "https://somwhere.com/, /article, https://somwhere.com/article",
      "https://somwhere.com/foo/bar, /article, https://somwhere.com/article",
      "https://somwhere.com/foo/bar, https://other.site/article, https://other.site/article",
      "https://somwhere.com/foo/bar/, ../article, https://somwhere.com/foo/article",
    ]
  )
  fun testAbsoluteUrl(base: String, link: String, expected: String) = runTest {
    assertThat(toAbsoluteUrl(URI(base), link).toURL().toString()).isEqualTo(expected)
  }

  private suspend fun testSupport(url: String, source: String) {
    val markup = readFile("${source}.input.html")
    val expected = readJson("${source}.output.json")
    val articles = getArticles(markup, URI(url))
    assertEquals(expected, articles.map { article -> article.url })
  }

  private suspend fun getArticles(html: String, url: URI): List<JsonItem> {
    val document = Jsoup.parse(html)

    val parserOptions = GenericFeedParserOptions()
    val rules = parser.parseFeedRules(document, url, parserOptions)
    assertThat(rules).isNotEmpty
    val bestRule = rules[0]
    val articles = parser.getArticlesBySelectors(bestRule, document, url)
    assertThat(articles).isNotEmpty
    return articles
  }

  private fun readFile(filename: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-websites/$filename").toPath())
  }

  private fun readJson(filename: String): List<String> {
    val gson = GsonBuilder().create()
    val raw = readFile(filename)
    return gson.fromJson<List<String>>(raw, List::class.java)
  }
}
