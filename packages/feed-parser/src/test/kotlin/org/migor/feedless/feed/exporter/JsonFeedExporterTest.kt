package org.migor.feedless.feed.exporter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonFeedExporterTest {

    @Test
    fun toJson() {
      val url = "https://foo.bar"
      val feed = createJsonFeed(url)

      val exporter = JsonFeedExporter()
      assertThat(exporter.toJson(feed)).isNotNull()
    }
}
