package org.migor.rich.rss.trigger

import org.migor.rich.rss.database.repository.ExporterRepository
import org.migor.rich.rss.harvest.ExporterHarvester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TriggerExporter internal constructor() {

  @Autowired
  lateinit var exporterRepository: ExporterRepository

  @Autowired
  lateinit var exporterHarvester: ExporterHarvester

  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun fillExporters() {
    exporterRepository.findDueToExporters(Date())
      .forEach { exporter -> exporterHarvester.harvestExporter(exporter) }
  }
}
