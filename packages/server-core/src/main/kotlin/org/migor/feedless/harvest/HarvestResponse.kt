package org.migor.feedless.harvest

import org.migor.feedless.common.HttpResponse

data class HarvestResponse(val url: String, val response: HttpResponse)
