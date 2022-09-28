package org.migor.rich.rss.trigger

import org.migor.rich.rss.database2.repositories.ExporterDAO
import org.migor.rich.rss.harvest.ExporterHarvester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("database2")
class TriggerExporter internal constructor() {

  @Autowired
  lateinit var exporterDAO: ExporterDAO

  @Autowired
  lateinit var exporterHarvester: ExporterHarvester

  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun fillExporters() {
    exporterDAO.findDueToExporters(Date())
      .forEach { exporter -> exporterHarvester.harvestExporter(exporter) }
  }
}
