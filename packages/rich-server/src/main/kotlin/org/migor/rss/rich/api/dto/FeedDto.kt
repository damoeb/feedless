package org.migor.rss.rich.api.dto

import java.util.*

data class FeedDto(val id: String?, val name: String?, val description: String?, var pubDate: Date?, var entries: List<SourceEntryDto?>? = null, var link: String? = null)
