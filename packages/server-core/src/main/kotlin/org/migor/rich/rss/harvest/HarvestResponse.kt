package org.migor.rich.rss.harvest

import org.asynchttpclient.Response

data class HarvestResponse(val url: String, val response: Response)
