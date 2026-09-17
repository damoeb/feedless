package org.migor.feedless.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.feed.parser.FeedType

class FeedUtilTest {

  @ParameterizedTest
  @CsvSource(
    value = [
      "text/xml, ATOM",
      "text/rss+xml, ATOM",
      "application/rdf+xml, ATOM",
      "application/rdf+xml; charset=UTF-8, ATOM",
      "application/rss+xml, ATOM",
      "application/atom+xml, ATOM",
      "application/atom+xml, ATOM",
      "application/xml, ATOM",
      "text/calendar; charset=UTF-8, CALENDAR",
      "text/html, ",
    ]
  )
  fun detectFeedType(mimeType: String, feedType: FeedType?) {
    assertThat(FeedUtil.detectFeedType(mimeType)).isEqualTo(feedType)
  }
}
