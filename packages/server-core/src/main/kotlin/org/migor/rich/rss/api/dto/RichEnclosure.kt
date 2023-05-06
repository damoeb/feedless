package org.migor.rich.rss.api.dto

import org.migor.rich.rss.feed.parser.json.JsonAttachment


class RichEnclosure() : JsonAttachment() {
  constructor(url: String, type: String, length: Long, duration: Long?) : this() {
    this.url = url
    this.type = type
    this.duration = duration
    this.size = length
  }
}
