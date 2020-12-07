package org.migor.rss.rich

import com.google.gson.GsonBuilder
import org.migor.rss.rich.dto.FeedDto
import org.springframework.http.ResponseEntity

object FeedExporter {
//  http://underpop.online.fr/j/java/help/modules-with-rome-xml-java.html.gz
  private val gson = GsonBuilder().create()

  fun toRss(feed: FeedDto): ResponseEntity<String> {
    val body = ""
    return ResponseEntity.ok()
      .header("Content-Type", "application/rss+xml")
      .body(body)
  }

  fun toAtom(feed: FeedDto): ResponseEntity<String> {
    val body = ""
    return ResponseEntity.ok()
      .header("Content-Type", "application/atom+xml")
      .body(body)
  }

  fun toJson(feed: FeedDto): ResponseEntity<String> {
    val body = gson.toJson(feed)
    return ResponseEntity.ok()
      .header("Content-Type", "application/json")
      .body(body)
  }

}
