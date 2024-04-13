package org.migor.feedless.web

import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.DateClaimer
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.util.ResourceUtils
import java.net.URL
import java.nio.file.Files
import java.util.*


internal class WebToFeedTransformerTest {

  private lateinit var parser: WebToFeedTransformer

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    Mockito.`when`(propertyService.locale).thenReturn(Locale.forLanguageTag("en"))
    Mockito.`when`(propertyService.apiGatewayUrl).thenReturn("http://localhost:8080")

    parser = WebToFeedTransformer(propertyService, WebToTextTransformer(), mock(DateClaimer::class.java))
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

  /*
    'https://bookmarks.kovah.de/guest/links',
    'https://bulletin.nu/',
    'https://lukesmith.xyz/',
    'https://blog.substack.com/',
    'https://www.slowernews.com/',
    'https://arxiv.org/search/?query=math&searchtype=all&source=header',
    'https://duckduckgo.com/html/?q=feynman',
    'https://github.blog/changelog/',
    'https://news.ycombinator.com/',
    'https://ebay.com/',
    'https://medium.com/',
   */

  @Test
  fun testWebiphanyIsSupported() {
    testSupport("https://webiphany.com/", "10-webiphany-com")
  }

  @Test
  fun testSpencermountaIsSupported() {
    testSupport("https://blog.spencermounta.in", "01-spencermounta-in")
  }

  @Test
  fun testSpotifyIsSupported() {
    testSupport("https://spotify.com", "02-spotify-com")
  }

  @Test
  @Disabled
  fun testTelepolisIsSupported() {
    testSupport("https://telepolis.de", "03-telepolis-de")
  }

  @Test
  fun testArzgIsSupported() {
    testSupport("https://arzg.github.io/lang", "04-arzg-github-io-lang")
  }

  @Test
  @Disabled
  fun testBrandonsmithIsSupported() {
    testSupport("https://www.brandonsmith.ninja", "05-www-brandonsmith-ninja")
  }

  @Test
  fun testJonBoIsSupported() {
    testSupport("https://jon.bo/posts", "06-jon-bo-posts")
  }

  @Test
  fun testPgIsSupported() {
    testSupport(
      "https://paulgraham.com",
      "00-paulgraham-com-articles",
    )
  }

  @Test
  fun testFoolIsSupported() {
    testSupport("https://www.fool.com/author/20415", "09-fool-com")
  }

  @Test
  @Disabled
  fun testAudacityIsSupported() {
    testSupport("https://www.audacityteam.org/posts/", "11-audacityteam-org")
  }

  @Test
  fun testGoogleBlogIsSupported() {
    testSupport(
      "https://cloud.google.com/blog",
      "13-google-cloud-blog"
    )
  }

  @Test
  fun testLinkAceIsSupported() {
    testSupport("https://demo.linkace.org/guest/links", "14-linkace-org")
  }

  @Test
  @Disabled
  fun testCraigslistIsSupported() {
    testSupport("https://abilene.craigslist.org", "07-craigslist")
  }

  @Test
  @Disabled
  fun testArxivIsSupported() {
    testSupport("https://arxiv.org/list/math.GN/recent", "08-arxiv-org", true)
  }

  @Test
  fun testEthzIsSupported() {
    testSupport("https://sph.ethz.ch/news", "12-sph-ethz-ch") // todo expand context
  }

  @Test
  @Disabled
  fun testLukeSmithIsSupported() {
    testSupport("https://lukesmith.xyz/articles", "14-lukesmith-xyz", true)
  }

  private fun testSupport(url: String, source: String, strictMode: Boolean = false) {
    val markup = readFile("${source}.input.html")
    val expected = readJson("${source}.output.json")
    val articles = getArticles(markup, URL(url), strictMode)
    assertEquals(expected, articles.map { article -> article.url })
  }

  fun getArticles(html: String, url: URL, strictMode: Boolean): List<RichArticle> {

    val document = Jsoup.parse(html)

    val parserOptions = GenericFeedParserOptions(strictMode = strictMode)
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
