package org.migor.feedless.web

import com.google.gson.GsonBuilder
import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.license.LicenseService
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.test.context.ActiveProfiles
import org.springframework.util.ResourceUtils
import java.net.URL
import java.nio.file.Files
import java.util.*


@SpringBootTest
@ActiveProfiles(profiles = ["test"])
@MockBeans(
  value = [
    MockBean(KotlinJdslJpqlExecutor::class),
    MockBean(LicenseService::class),
  ]
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
  fun generalizeXPathsSimple() {
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
  fun generalizeXPathsComplex() {
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

  @ParameterizedTest
  @CsvSource(value = [
    "https://webiphany.com/, 10-webiphany-com",
    "https://blog.spencermounta.in, 01-spencermounta-in",
    "https://spotify.com, 02-spotify-com",
    "https://telepolis.de, 03-telepolis-de",
//    "https://arzg.github.io/lang, 04-arzg-github-io-lang",
    "https://www.brandonsmith.ninja, 05-www-brandonsmith-ninja",
//    "https://jon.bo/posts, 06-jon-bo-posts",
//    "https://paulgraham.com, 00-paulgraham-com-articles",
//    "https://www.fool.com/author/20415, 09-fool-com",
    "https://www.audacityteam.org/posts/, 11-audacityteam-org",
//    "https://cloud.google.com/blog, 13-google-cloud-blog",
    "https://demo.linkace.org/guest/links, 14-linkace-org",
//    "https://abilene.craigslist.org, 07-craigslist",
//    "https://arxiv.org/list/math.GN/recent, 08-arxiv-org",
    "https://sph.ethz.ch/news, 12-sph-ethz-ch", // todo expand context
//    "https://lukesmith.xyz/articles, 14-lukesmith-xyz",
  ])
  fun testSiteIsSupported(url: String, id: String) {
    testSupport(url, id)
  }

  private fun testSupport(url: String, source: String) {
    val markup = readFile("${source}.input.html")
    val expected = readJson("${source}.output.json")
    val articles = getArticles(markup, URL(url))
    assertEquals(expected, articles.map { article -> article.url })
  }

  private fun getArticles(html: String, url: URL): List<JsonItem> {

    val document = Jsoup.parse(html)

    val parserOptions = GenericFeedParserOptions()
    val rules = parser.parseFeedRules("-", document, url, parserOptions)
    if (rules.isEmpty()) {
      throw RuntimeException("No rules available")
    }
    val bestRule = rules[0]
    return parser.getArticlesBySelectors("-", bestRule, document, url)
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
