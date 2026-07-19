package org.migor.feedless.http.mapper

import org.migor.feedless.harvest.Harvest
import org.migor.feedless.util.toMillis
import org.springframework.stereotype.Component
import org.migor.feedless.http.api.model.Harvest as HttpHarvest

@Component
class HttpHarvestMapper {

  fun toHttp(harvest: Harvest, includeLogs: Boolean): HttpHarvest =
    HttpHarvest(
      ok = !harvest.errornous,
      itemsAdded = harvest.itemsAdded,
      itemsIgnored = harvest.itemsIgnored,
      logs = if (includeLogs) harvest.logs else "",
      startedAt = harvest.startedAt.toMillis(),
      finishedAt = harvest.finishedAt?.toMillis(),
    )
}
