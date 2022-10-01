package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.ExporterTargetEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExporterTargetDAO : CrudRepository<ExporterTargetEntity, UUID> {
  fun findAllByExporterId(id: UUID): List<ExporterTargetEntity>
}
