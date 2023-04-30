package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.HyperLinkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface HyperLinkDAO : JpaRepository<HyperLinkEntity, UUID> {
  fun deleteAllByFromId(fromId: UUID)
}
