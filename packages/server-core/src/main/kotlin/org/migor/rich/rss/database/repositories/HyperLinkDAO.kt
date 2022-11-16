package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.HyperLinkEntity
import org.springframework.data.repository.CrudRepository
import java.util.*


interface HyperLinkDAO : CrudRepository<HyperLinkEntity, UUID> {
  fun deleteAllByFromIdIn(fromIds: List<UUID>)
}
