package org.migor.rich.rss.api.dto

import org.migor.rich.rss.harvest.feedparser.json.GenericFeed
import org.migor.rich.rss.harvest.feedparser.json.JsonFeed

class RichFeed(): GenericFeed<RichArticle>() {

  var selfPage: Int? = null
  var lastPage: Int? = null

  constructor(feed: JsonFeed): this() {
    id = feed.id
    title = feed.title
    iconUrl = feed.iconUrl
    version = feed.version
    favicon = feed.favicon
    description = feed.description
    author = feed.author
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
  }
}
