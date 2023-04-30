package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.AttachmentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AttachmentDAO : JpaRepository<AttachmentEntity, UUID>, PagingAndSortingRepository<AttachmentEntity, UUID> {
  fun findAllByWebDocumentId(webDocumentId: UUID): List<AttachmentEntity>
}
