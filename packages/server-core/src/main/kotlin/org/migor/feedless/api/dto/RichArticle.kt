package org.migor.feedless.api.dto

import org.migor.feedless.feed.parser.json.JsonItem

class RichArticle() : JsonItem() {
  constructor(item: JsonItem) : this() {
    id = item.id
    title = item.title
    url = item.url
    tags = item.tags
    contentText = item.contentText
    contentRawBase64 = item.contentRawBase64
    contentRawMime = item.contentRawMime
    summary = item.summary
    imageUrl = item.imageUrl
    bannerImage = item.bannerImage
    language = item.language
    authors = item.authors
    attachments = item.attachments
    publishedAt = item.publishedAt
    startingAt = item.startingAt
    modifiedAt = item.modifiedAt
    commentsFeedUrl = item.commentsFeedUrl
  }
}
