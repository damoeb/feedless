package org.migor.rss.rich.harvest

import org.asynchttpclient.Response

data class HarvestResponse(val url: HarvestUrl, val response: Response) {
  constructor(url: String, response: Response) : this(HarvestUrl(url), response)
}
