package org.migor.rss.rich.dto

import org.migor.rss.rich.model.SourceStatus
import java.util.*

data class SourceDto(val id: String?, val title: String?, val description: String?, val status: SourceStatus, var lastUpdatedAt: Date)
