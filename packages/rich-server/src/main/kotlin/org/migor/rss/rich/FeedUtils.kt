package org.migor.rss.rich

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object FeedUtils {
  private val uriDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  fun toURI(entryId: String, subscriptionId: String, createdAt: Date): String {
    // example tag:diveintomark.org,2004-05-27:1192 from https://web.archive.org/web/20080701231200/http://diveintomark.org/archives/2004/05/28/howto-atom-id
    return "tag:rich-rss.migor.org,${LocalDateTime.from(createdAt.toInstant()).format(uriDateFormatter)}:subscription:${subscriptionId}/entry:${entryId}"
  }

}
