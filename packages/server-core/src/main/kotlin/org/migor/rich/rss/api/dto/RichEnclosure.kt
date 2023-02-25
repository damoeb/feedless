package org.migor.rich.rss.api.dto

import org.migor.rich.rss.harvest.feedparser.json.JsonAttachment

class RichEnclosure() : JsonAttachment() {
  constructor(url: String, type: String, length: Long) : this() {
    this.url = url
    this.type = type
    this.length = length
  }
}
