package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.data.jpa.models.HyperLinkEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*


interface HyperLinkDAO : JpaRepository<HyperLinkEntity, UUID> {
  fun deleteAllByFromId(fromId: UUID)
}
