package org.migor.rss.rich.dtos

import org.migor.rss.rich.models.SubscriptionStatus

data class SubscriptionDto(var uuid: String?,
                           var name: String?,
                           var status: SubscriptionStatus?,
                           var ownerId: String?,
                           var harvestFrequency: HarvestFrequencyDto?) {

}
