package org.migor.rss.rich.api.dto

import java.util.Date


data class ArticleJsonDto(val id: String, val title: String, val tags: Collection<String>?, val content_text: String, val content_html: String?, val url: String, val author: String?, val enclosures: String?, val date_published: Date, val commentsFeedUrl: String?)
