package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.ExporterTarget
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ExporterTargetRepository : CrudRepository<ExporterTarget, String> {
  fun findAllByExporterId(id: String): List<ExporterTarget>
}
