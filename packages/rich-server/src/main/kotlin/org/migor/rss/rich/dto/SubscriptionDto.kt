package org.migor.rss.rich.dto

import org.migor.rss.rich.model.SubscriptionStatus

data class SubscriptionDto(var id: String?,
                           var name: String?,
                           var status: SubscriptionStatus?,
                           var ownerId: String?,
                           var harvestFrequency: HarvestFrequencyDto?) {

}
