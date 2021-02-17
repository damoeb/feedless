package org.migor.rss.rich.dto

data class SubscriptionGroupDto(var id: String?,
                                var name: String?,
                                var ownerId: String?,
                                var order: Int?,
                                var subscriptions: List<SubscriptionDto>? = null)
