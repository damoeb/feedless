package org.migor.feedless.feed.parser

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.common.HttpResponse
import org.springframework.util.ResourceUtils
import java.nio.file.Files

class XmlFeedParserTest {

  private val corrId = "test"

  private fun readFile(ref: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:transform/$ref").toPath())
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "after-on-rss.in.xml",
      "apple-rss.in.xml",
      "medium-rss.in.xml",
      "yt-atom.in.xml"
    ]
  )
  fun `parse feed from xml`(feedFileName: String) = runTest {
    val parser = XmlFeedParser()

    val httpResponse = HttpResponse(
      contentType = "application/xml",
      url = "",
      statusCode = 200,
      responseBody = ResourceUtils.getFile("classpath:transform/$feedFileName").readBytes(),
    )

    val feed = parser.process(corrId, httpResponse)
    assertThat(feed).isNotNull()
  }

}
