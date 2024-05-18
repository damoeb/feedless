package org.migor.feedless.api.dto

import org.migor.feedless.feed.parser.json.GenericFeed
import org.migor.feedless.feed.parser.json.JsonFeed


class RichFeed() : GenericFeed<RichArticle>() {

  constructor(feed: JsonFeed) : this() {
    id = feed.id
    title = feed.title
    iconUrl = feed.iconUrl
    version = feed.version
    favicon = feed.favicon
    description = feed.description
    authors = feed.authors
    websiteUrl = feed.websiteUrl
    user_comment = feed.user_comment
    imageUrl = feed.imageUrl
    language = feed.language
    publishedAt = feed.publishedAt
    items = feed.items.map { RichArticle(it) }
    feedUrl = feed.feedUrl
    expired = feed.expired
    tags = feed.tags
    nextUrl = feed.nextUrl
    previousUrl = feed.previousUrl
  }
}
