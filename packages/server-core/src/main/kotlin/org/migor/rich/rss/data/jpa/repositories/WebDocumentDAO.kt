package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface WebDocumentDAO : JpaRepository<WebDocumentEntity, UUID> {
  fun findByUrlEquals(url: String): Optional<WebDocumentEntity>
  fun existsByUrl(url: String): Boolean

  @Query(
    """select WD from WebDocumentEntity WD
      inner join HyperLinkEntity HL on HL.toId = WD.id
      where HL.fromId = ?1 and WD.finished = true"""
  )
  fun findAllOutgoingHyperLinksByContentId(fromId: UUID, pageable: Pageable): List<WebDocumentEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
  override fun deleteById(id: UUID)
}
