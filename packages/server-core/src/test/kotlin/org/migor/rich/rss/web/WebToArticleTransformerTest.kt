package org.migor.rich.rss.web

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.migor.rich.rss.util.JsonUtil
import org.springframework.util.ResourceUtils
import java.nio.file.Files

// todo fix
internal class WebToArticleTransformerTest {

  private lateinit var extractor: WebToArticleTransformer

  @BeforeEach
  fun up() {
    extractor = WebToArticleTransformer(MarkupSimplifier())
  }

  @Test
  @Disabled
  fun verify_derstandard_at_isSupported() {
    doExtract("derstandard_at", "https://derstandard.at")
  }

  @Test
  @Disabled
  fun verify_newyorker_com_isSupported() {
    doExtract("newyorker_com", "https://www.newyorker.com")
  }

  @Test
  @Disabled
  fun verify_spiegel_de_isSupported() {
    doExtract("spiegel_de", "https://www.spiegel.de")
  }

  @Test
  @Disabled
  fun verify_theatlantic_com_isSupported() {
    doExtract("theatlantic_com", "https://www.theatlantic.com")
  }

  @Test
  @Disabled
  fun verify_diepresse_com_isSupported() {
    doExtract("diepresse_com", "https://www.diepresse.com")
  }

  @Test
  @Disabled
  fun verify_medium_com_isSupported() {
    doExtract("medium_com", "https://www.medium.com")
  }

  @Test
  @Disabled
  fun verify_wikipedia_org_isSupported() {
    doExtract("wikipedia_org", "https://www.wikipedia.org")
  }

  @Test
  @Disabled
  fun verify_wordpress_com_isSupported() {
    doExtract("wordpress_com", "https://www.wikipedia.org")
  }

  private fun doExtract(ref: String, url: String) {
    val actual = extractor.fromHtml(readFile("${ref}.html"), url)
    val expected = JsonUtil.gson.fromJson(readFile("${ref}.json"), ExtractedArticle::class.java)
    Assertions.assertEquals(expected.content, actual.content)
    Assertions.assertEquals(expected.contentText, actual.contentText)
    Assertions.assertEquals(expected.date, actual.date)
    Assertions.assertEquals(expected.originalUrl, actual.originalUrl)
    Assertions.assertEquals(expected.faviconUrl, actual.faviconUrl)
    Assertions.assertEquals(expected.title, actual.title)
  }

  private fun readFile(ref: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-articles/$ref").toPath())
  }
}
