package org.migor.rss.rich.dto

data class SubscriptionGroupDto(var id: String?,
                                var name: String?,
                                var ownerId: String?,
                                var lastUpdatedAtAgo: String? = null,
                                var order: Int?,
                                var subscriptions: List<SubscriptionDto?>? = null,
                                var entries: List<SourceEntryDto?>? = null)
