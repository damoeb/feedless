package org.migor.feedless.web

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.util.JsonUtil
import org.springframework.util.ResourceUtils
import java.nio.file.Files

internal class WebToArticleTransformerTest {

  private lateinit var extractor: WebToArticleTransformer

  @BeforeEach
  fun setUp() {
    extractor = WebToArticleTransformer()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "derstandard_at, https://derstandard.at",
      "newyorker_com, https://www.newyorker.com",
      "spiegel_de, https://www.spiegel.de",
      "theatlantic_com, https://www.theatlantic.com",
      "diepresse_com, https://www.diepresse.com",
      "medium_com, https://www.medium.com",
      "wordpress_com, https://www.wordpress.com",
      "wikipedia_org, https://www.wikipedia.org"
    ]
  )
  fun verify_site_isSupported(name: String, baseUrl: String) {
    doExtract(name, baseUrl)
  }

  private fun doExtract(ref: String, url: String) {
    runBlocking {
      val actual = extractor.fromHtml(readFile("${ref}.html"), url)
      val expected = JsonUtil.gson.fromJson(readFile("${ref}.json"), JsonItem::class.java)
//    assertThat(expected.contentHtml).isEqualTo(actual.contentHtml)
//    assertThat(expected.contentText).isEqualTo(actual.contentText)
//    assertThat(expected.publishedAt).isEqualTo(actual.publishedAt)
      assertThat(expected.url).isEqualTo(actual.url)
      assertThat(expected.title).isEqualTo(actual.title)
//    assertThat(expected.faviconUrl).isEqualTo(actual.faviconUrl)
    }
  }

  private fun readFile(ref: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-articles/$ref").toPath())
  }
}
