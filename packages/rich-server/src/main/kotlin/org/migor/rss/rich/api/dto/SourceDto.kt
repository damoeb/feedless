package org.migor.rss.rich.api.dto

import org.migor.rss.rich.database.model.SourceStatus
import java.util.*

data class SourceDto(val id: String?, val title: String?, val description: String?, val status: SourceStatus, var lastUpdatedAt: Date?, var url: String?, var throughput: String)
