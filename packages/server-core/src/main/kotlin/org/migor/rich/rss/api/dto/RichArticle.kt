package org.migor.rich.rss.api.dto

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.feed.parser.json.JsonItem

class RichArticle() : JsonItem() {
  constructor(item: JsonItem) : this() {
    id = item.id
    title = item.title
    url = item.url
    tags = item.tags
    contentText = item.contentText
    contentHtml = item.contentHtml
    summary = item.summary
    imageUrl = item.imageUrl
    bannerImage = item.bannerImage
    language = item.language
    author = item.author
    authors = item.authors
    attachments = item.attachments
    publishedAt = item.publishedAt
    startingAt = item.startingAt
    modifiedAt = item.modifiedAt
    startingAt = item.startingAt
    commentsFeedUrl = item.commentsFeedUrl
  }

  override var contentHtml: String?
    get() = super.contentHtml
    set(value) {
      if (StringUtils.isBlank(value)) {
        contentRaw = value
        contentRawMime = "text/html"
      } else {
        contentRaw = null
        contentRawMime = null
      }
    }

  var contentRaw: String? = null
  var contentRawMime: String? = null
}
