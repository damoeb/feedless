package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.AttachmentEntity
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AttachmentDAO : PagingAndSortingRepository<AttachmentEntity, UUID>
