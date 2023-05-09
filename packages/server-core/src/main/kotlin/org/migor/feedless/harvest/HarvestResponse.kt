package org.migor.feedless.harvest

import org.migor.feedless.service.HttpResponse

data class HarvestResponse(val url: String, val response: HttpResponse)
