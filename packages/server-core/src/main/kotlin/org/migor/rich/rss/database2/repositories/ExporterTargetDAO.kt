package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.ExporterTargetEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExporterTargetDAO : CrudRepository<ExporterTargetEntity, UUID> {
  fun findAllByExporterId(id: UUID): List<ExporterTargetEntity>
}
