package org.migor.feedless.trigger

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.harvest.HarvestAbortedException
import org.migor.feedless.harvest.ResumableHarvestException
import org.migor.feedless.harvest.ServiceUnavailableException
import org.migor.feedless.harvest.SiteNotFoundException
import org.migor.feedless.trigger.plugins.WebDocumentPlugin
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile("${AppProfiles.database} && !${AppProfiles.testing}")
@Transactional(propagation = Propagation.NEVER)
class TriggerPlugins internal constructor() {

  private val log = LoggerFactory.getLogger(TriggerPlugins::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var allPlugins: List<WebDocumentPlugin>

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun executePlugins() {
    val pageable = PageRequest.of(0, 20, Sort.Direction.ASC, StandardJpaFields.createdAt)
    val corrId = newCorrId()
    webDocumentDAO.saveAll(webDocumentDAO.findNextUnfinalized(pageable)
      .map { handle(newCorrId(3, corrId), it) })
  }

  private fun handle(corrId: String, webDocument: WebDocumentEntity): WebDocumentEntity {
    val pendingPlugins = webDocument.pendingPlugins.toMutableList()
    val executedPlugins = webDocument.executedPlugins.toMutableList()
    webDocument.pendingPlugins
      .mapNotNull { resolvePlugin(it) }
      .sortedBy { it.executionPhase() }
      .takeWhile {
        runCatching {
          log.debug("[$corrId] plugin ${it.id()} for ${webDocument.id}")
          it.processWebDocument(corrId, webDocument)
          pendingPlugins.remove(it.id())
          executedPlugins.add(it.id())
          true
        }.recover { ex ->
          when (ex) {
            is SiteNotFoundException,
            is ServiceUnavailableException,
            is HarvestAbortedException -> {
              pendingPlugins.remove(it.id())
              log.info("[$corrId] Skipping step ${it.id()} cause ${ex.message}")
              true
            }
            is ResumableHarvestException -> {
              log.info("[$corrId] postponing (${it::class.simpleName}): ${ex.message}")
              webDocument.pluginsCoolDownUntil = Date(System.currentTimeMillis() + ex.nextRetryAfter.toMillis())
              false
            }
            else -> {
              pendingPlugins.clear()
              log.warn("[$corrId] aborting cause ${ex.message}")
              false
            }
          }
        }.getOrElse { false }
      }

    webDocument.pendingPlugins = pendingPlugins
    webDocument.executedPlugins = executedPlugins
    webDocument.finalized = pendingPlugins.isEmpty()
    if (webDocument.finalized) {
      log.info("[$corrId] plugins finalized ${webDocument.id}")
    } else {
      log.info("[$corrId] plugins remaining=[${pendingPlugins.joinToString(",")}] for webDocument ${webDocument.id}")
    }
    return webDocument
  }

  private fun resolvePlugin(name: String): WebDocumentPlugin? {
    return this.allPlugins.sortedBy { it.executionPhase() }.find { it.id() == name }
      .also { it ?: log.warn("invalid plugin $name") }
  }
}
