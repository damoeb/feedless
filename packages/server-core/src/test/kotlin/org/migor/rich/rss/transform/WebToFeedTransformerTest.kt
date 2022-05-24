package org.migor.rich.rss.transform

import com.google.gson.GsonBuilder
import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.rich.rss.api.dto.ArticleJsonDto
import org.migor.rich.rss.harvest.ArticleRecovery
import org.migor.rich.rss.service.PropertyService
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
    Mockito.`when`(propertyService.host).thenReturn("http://localhost:8080")

    parser = WebToFeedTransformer(propertyService, WebToTextTransformer())
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

    assertEquals("body/table[1]/tbody[1]/tr[1]/td[3]/table/tbody[1]/tr/td[1]/font[1]/a", parser.__generalizeXPaths(xpaths))
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
    assertEquals("div[contains(id, 'democracy') or contains(id, 'economy') or contains(id, 'health')]/ul[1]/li", parser.__generalizeXPaths(xpaths))
  }

  @Test
  fun testSupportedSites() {
    val sites = listOf(
//      Pair("https://webiphany.com/", "10-webiphany-com"),
//      Pair("https://blog.spencermounta.in", "01-spencermounta-in"),
//      Pair("https://spotify.com", "02-spotify-com"),
//      Pair("https://telepolis.de", "03-telepolis-de"),
//      Pair("https://arzg.github.io", "04-arzg-github-io-lang"),
      Pair("https://www.brandonsmith.ninja", "05-www-brandonsmith-ninja"),
//      Pair("https://jon.bo", "06-jon-bo-posts"),
//      Pair("https://arxiv.org", "08-arxiv-org"),
    )
    sites.forEach { site ->
      run {
        val markup = readFile("${site.second}.input.html")
        val expected = readJson("${site.second}.output.json")
        val articles = getArticles(markup, URL(site.first))
        assertEquals(expected, articles.map { article -> article.url })
      }
    }
  }

  fun getArticles(html: String, url: URL): List<ArticleJsonDto> {

    val document = Jsoup.parse(html)

    val rules = parser.getArticleRules("-", document, url, ArticleRecovery.NONE)
    if (rules.isEmpty()) {
      throw RuntimeException("No rules available")
    }
    val bestRule = rules[0]
    return parser.getArticlesByRule("-", bestRule, document, url)
  }

  @Test
  fun testDateExtractor() {
    val dateStrings = listOf(
      Triple("2022-01-08T00:00:00", "Sat Jan 08 00:00:00 CET 2022", null),
//      Triple("06. Januar 2022, 08:00 Uhr", "", Locale.GERMAN),
      Triple("06. Januar 2022, 08:00", "Thu Jan 06 08:00:00 CET 2022", Locale.GERMAN),
//      Triple("Heute, 08:00 Uhr", Locale.GERMAN, ""),
      Triple("October 9, 2019", "Wed Oct 09 08:00:00 CEST 2019", Locale.ENGLISH),
      Triple("December 15, 2020", "Tue Dec 15 08:00:00 CET 2020", Locale.ENGLISH),
      Triple("Dezember 15, 2020", "Tue Dec 15 08:00:00 CET 2020", Locale.GERMAN),
      Triple("2022-04-28T15:50:21-07:00", "Thu Apr 28 15:50:21 CEST 2022", Locale.GERMAN),
    )
    dateStrings.forEach { (dateStr, expected, locale) ->
      run {
        val actual = parser.parseDateFromTimeElement("-", dateStr, locale)
        assertEquals(expected, actual.toString())
      }
    }
  }

  @Test
  @Disabled
  fun testYetUnsupportedSites() {
    val sites = listOf(
      Pair(
        "https://paulgraham.com",
        "00-paulgraham-com-articles",
      ),
      Pair("https://abilene.craigslist.org", "07-craigslist"),
      Pair("https://www.fool.com/author/20415", "09-fool-com"),
      Pair("https://www.audacityteam.org/posts/", "11-audacityteam-org"),
    )
    sites.forEach { site ->
      run {
        val markup = readFile("${site.second}.input.html")
        val expected = readJson("${site.second}.output.json")
        val articles = getArticles(markup, URL(site.first))
        assertEquals(expected, articles.map { article -> article.url })
      }
    }
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
