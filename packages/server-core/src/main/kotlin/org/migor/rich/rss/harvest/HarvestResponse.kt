package org.migor.rich.rss.harvest

import org.migor.rich.rss.service.HttpResponse

data class HarvestResponse(val url: String, val response: HttpResponse)
