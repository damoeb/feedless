package org.migor.rss.rich.dto

import org.migor.rss.rich.model.SourceStatus
import java.util.*

data class SubscriptionDto(var id: String?,
                           var title: String?,
                           var description: String?,
                           var lastUpdatedAt: Date?,
                           var url: String?,
                           var throttled: Boolean?,
                           var throughput: String,
                           var sourceId: String?,
                           var status: SourceStatus?,
                           var groupId: String?,
                           var ownerId: String?,
                           var throttle: ThrottleDto)
