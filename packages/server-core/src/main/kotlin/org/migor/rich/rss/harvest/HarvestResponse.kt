package org.migor.rss.rich.harvest

import org.asynchttpclient.Response

data class HarvestResponse(val url: String, val response: Response)
