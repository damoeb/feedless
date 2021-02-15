package org.migor.rss.rich.dto

import java.util.*

data class FeedDto(val id: String?, val name: String?, val description: String?, var pubDate: Date?, var ownerId: String?)
