package org.migor.rss.rich.dto

data class SubscriptionGroupDto(var id: String?,
                                var name: String?,
                                var filtered: Boolean?,
                                var ownerId: String?,
                                var subscriptions: Int? = null)
