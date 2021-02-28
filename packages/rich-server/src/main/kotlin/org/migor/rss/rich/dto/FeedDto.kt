package org.migor.rss.rich.dto

import org.migor.rss.rich.model.AccessPolicy
import java.util.*

data class FeedDto(val id: String?, val name: String?, val description: String?, var pubDate: Date?, var ownerId: String?, var accessPolicy: AccessPolicy?, var entries: List<SourceEntryDto?>? = null, var link: String? = null)
