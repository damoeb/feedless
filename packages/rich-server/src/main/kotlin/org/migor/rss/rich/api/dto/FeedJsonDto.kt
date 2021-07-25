package org.migor.rss.rich.api.dto

import java.util.*

data class FeedJsonDto(val id: String?, val name: String?, val description: String?, val home_page_url: String?, var date_published: Date?, var items: List<ArticleJsonDto?>? = null, var feed_url: String? = null, val expired: Boolean)
