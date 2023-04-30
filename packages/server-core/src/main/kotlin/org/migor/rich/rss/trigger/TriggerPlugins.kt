package org.migor.rich.rss.trigger

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.models.WebDocumentEntity
import org.migor.rich.rss.data.jpa.repositories.WebDocumentDAO
import org.migor.rich.rss.harvest.HarvestAbortedException
import org.migor.rich.rss.harvest.ResumableHarvestException
import org.migor.rich.rss.harvest.ServiceUnavailableException
import org.migor.rich.rss.harvest.SiteNotFoundException
import org.migor.rich.rss.trigger.plugins.WebDocumentPlugin
import org.migor.rich.rss.util.CryptUtil.newCorrId
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
@Profile(AppProfiles.database)
@Transactional(propagation = Propagation.NEVER)
class TriggerPlugins internal constructor() {

  private val log = LoggerFactory.getLogger(TriggerPlugins::class.simpleName)

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var allPlugins: List<WebDocumentPlugin>

  @Scheduled(fixedDelay = 3245)
  @Transactional
  fun executePlugins() {
    val pageable = PageRequest.of(0, 20, Sort.Direction.ASC, StandardJpaFields.createdAt)
    val corrId = newCorrId()
    webDocumentDAO.saveAll(webDocumentDAO.findNextUnfinalized(pageable)
      .map { handle(newCorrId(3, corrId), it) })
  }

  private fun handle(corrId: String, webDocument: WebDocumentEntity): WebDocumentEntity {
    runCatching {
      webDocument.plugins
        .mapNotNull { resolvePlugin(it) }
        .sortedBy { it.executionPriority() }
        .forEach {
          log.info("[$corrId] plugin ${it.id()} for ${webDocument.id}")
          runCatching {
            it.processWebDocument(corrId, webDocument)
          }.onFailure { ex -> when(ex) {
            is SiteNotFoundException,
            is ServiceUnavailableException,
            is HarvestAbortedException -> log.warn("[$corrId] ${ex.message}")
            else -> throw ex
          }
          }
        }
      webDocument.plugins = emptyList()
      webDocument.pluginsCoolDownUntil = null
      webDocument.finalized = true
      log.info("[$corrId] ${webDocument.id} plugins finalized")
    }.onFailure {
      when(it) {
        is ResumableHarvestException -> {
          log.info("[$corrId] postponed harvest (${it::class.simpleName})")
          webDocument.pluginsCoolDownUntil = Date(System.currentTimeMillis() + it.nextRetryAfter.toMillis())
        }

        else -> {
          it.printStackTrace()
          log.warn("[${corrId}] Failed to extract: ${it.message}")
          webDocument.pluginsCoolDownUntil = null
          webDocument.finalized = true
        }
      }
    }
    return webDocument
  }

  private fun resolvePlugin(name: String): WebDocumentPlugin? {
    return this.allPlugins.sortedBy { it.executionPriority() }.find { it.id() == name }.also { it?: log.warn("unknown plugin $name") }
  }
}
