package org.migor.rich.rss.feed.exporter

import com.rometools.rome.io.SyndFeedOutput
import org.migor.rich.rss.api.dto.RichFeed
import org.migor.rich.rss.util.FeedUtil
import org.springframework.stereotype.Service


@Service
class SyndAtomFeedExporter {
  fun toAtom(corrId: String, r: RichFeed): String {
    val output = SyndFeedOutput()
    return output.outputString(FeedUtil.toSyndFeed(r))
  }
}
