package org.migor.rss.rich.trigger

import org.migor.rss.rich.database.repository.ExporterRepository
import org.migor.rss.rich.harvest.ExporterHarvester
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class TriggerExporter internal constructor() {

  @Autowired
  lateinit var exporterRepository: ExporterRepository

  @Autowired
  lateinit var exporterHarvester: ExporterHarvester

//  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun fillExporters() {
    exporterRepository.findDueToExporters(Date())
      .forEach { exporter -> exporterHarvester.harvestExporter(exporter) }
  }
}
