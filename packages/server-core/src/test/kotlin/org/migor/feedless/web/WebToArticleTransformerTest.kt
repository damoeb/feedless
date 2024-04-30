package org.migor.feedless.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
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
  @CsvSource(value = [
    "derstandard_at, https://derstandard.at",
    "newyorker_com, https://www.newyorker.com",
    "spiegel_de, https://www.spiegel.de",
    "theatlantic_com, https://www.theatlantic.com",
    "diepresse_com, https://www.diepresse.com",
    "medium_com, https://www.medium.com",
    "wordpress_com, https://www.wordpress.com",
    "wikipedia_org, https://www.wikipedia.org"
  ])
  fun verify_site_isSupported(name: String, baseUrl: String) {
    doExtract(name, baseUrl)
  }

  private fun doExtract(ref: String, url: String) {
    val actual = extractor.fromHtml(readFile("${ref}.html"), url)
    val expected = JsonUtil.gson.fromJson(readFile("${ref}.json"), ExtractedArticle::class.java)
    // todo fix
//    assertThat(expected.content).isEqualTo(actual.content)
//    assertThat(expected.contentText).isEqualTo(actual.contentText)
    assertThat(expected.date).isEqualTo(actual.date)
    assertThat(expected.originalUrl).isEqualTo(actual.originalUrl)
    assertThat(expected.title).isEqualTo(actual.title)
//    assertThat(expected.faviconUrl).isEqualTo(actual.faviconUrl)
  }

  private fun readFile(ref: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-articles/$ref").toPath())
  }
}
