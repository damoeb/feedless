package org.migor.rss.rich.transform

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.rss.rich.service.PropertyService
import org.mockito.Mockito.mock
import org.springframework.util.ResourceUtils
import java.net.URL
import java.nio.file.Files


internal class WebToFeedParserTest {

  private lateinit var parser: WebToFeedParser

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    propertyService.host = "http://localhost:8080"
    parser = WebToFeedParser(propertyService, WebToTextTransformer())
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

    assertEquals("body/table[1]/tbody[1]/tr[1]/td[3]/table/tbody[1]/tr/td[1]/font[1]/a", parser.generalizeXPaths(xpaths))
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
    assertEquals("div[contains(id, 'democracy') or contains(id, 'economy') or contains(id, 'health')]/ul[1]/li", parser.generalizeXPaths(xpaths))
  }

  @Test
  fun testSupportedSites() {
    val sites = listOf(
      Pair("https://webiphany.com/", "10-webiphany-com"),
      Pair("https://blog.spencermounta.in", "01-spencermounta-in"),
      Pair("https://spotify.com", "02-spotify-com"),
      Pair("https://telepolis.de", "03-telepolis-de"),
      Pair("https://arzg.github.io", "04-arzg-github-io-lang"),
      Pair("https://www.brandonsmith.ninja", "05-www-brandonsmith-ninja"),
      Pair("https://jon.bo", "06-jon-bo-posts"),
      Pair("https://arxiv.org", "08-arxiv-org"),
    )
    sites.forEach { site ->
      run {
        val markup = readFile("${site.second}.input.html")
        val expected = readJson("${site.second}.output.json")
        val articles = parser.getArticles(markup, URL(site.first))
        assertEquals(expected, articles.map { article -> article.url })
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
        val articles = parser.getArticles(markup, URL(site.first))
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
