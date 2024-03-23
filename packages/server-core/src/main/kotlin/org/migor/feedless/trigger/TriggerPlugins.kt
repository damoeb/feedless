package org.migor.feedless.trigger

import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.MailForwardEntity
import org.migor.feedless.data.jpa.models.PipelineJobEntity
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.MailForwardDAO
import org.migor.feedless.data.jpa.repositories.PipelineJobDAO
import org.migor.feedless.data.jpa.repositories.SourceSubscriptionDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.plugins.FeedlessPlugin
import org.migor.feedless.plugins.FilterEntityPlugin
import org.migor.feedless.plugins.MapEntityPlugin
import org.migor.feedless.service.MailService
import org.migor.feedless.service.PluginService
import org.migor.feedless.util.CryptUtil.newCorrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.ArrayList

@Service
@Profile(AppProfiles.database)
@Transactional(propagation = Propagation.NEVER)
class TriggerPlugins internal constructor() {

  private val log = LoggerFactory.getLogger(TriggerPlugins::class.simpleName)

  @Autowired
  lateinit var pipelineJobDAO: PipelineJobDAO

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var sourceSubscriptionDAO: SourceSubscriptionDAO

  @Autowired
  lateinit var pluginService: PluginService

  @Autowired
  lateinit var mailService: MailService

  @Autowired
  lateinit var mailForwardDAO: MailForwardDAO

  @Scheduled(fixedDelay = 3245, initialDelay = 20000)
  @Transactional
  fun executePlugins() {
    val corrId = newCorrId()
    pipelineJobDAO.findAllPendingBatched()
      .groupBy { it.webDocumentId }
      .map { processPipelineJob(newCorrId(3, corrId), it.key, it.value) }
  }

  private fun processPipelineJob(corrId: String, webDocumentId: UUID, jobs: List<PipelineJobEntity>) {
    log.info("[$corrId] ${jobs.size} processPipelineJobs for webDocument $webDocumentId")
    val webDocument = webDocumentDAO.findById(webDocumentId).orElseThrow()
    val subscription = sourceSubscriptionDAO.findById(webDocument.subscriptionId).orElseThrow()
    try {
      var omitted = false
      for (job in jobs) {
        try {
          if (job.attempt > 3) {
            throw IllegalArgumentException("max attempts reached")
          }

          val plugin = pluginService.resolveById<FeedlessPlugin>(job.executorId)
          when (plugin) {
            is FilterEntityPlugin -> if (!plugin.filterEntity(corrId, webDocument, job.executorParams)) {
              omitted = true
              break
            }

            is MapEntityPlugin -> plugin.mapEntity(corrId, webDocument, subscription, job.executorParams)
            else -> throw IllegalArgumentException("Invalid executorId ${job.executorId}")
          }
          job.terminated = true
          pipelineJobDAO.save(job)

        } catch (e: ResumableHarvestException) {
          log.info("[$corrId] delaying (${job.executorId}): ${e.message}")
          job.coolDownUntil = Date(System.currentTimeMillis() + e.nextRetryAfter.toMillis())
          job.attempt = job.attempt + 1
          pipelineJobDAO.save(job)
          break
        }
      }
      if (omitted) {
        webDocumentDAO.delete(webDocument)
      } else {
        forwardToMail(corrId, webDocument, subscription)
        webDocument.status = ReleaseStatus.released
        webDocumentDAO.save(webDocument)
      }

    } catch (throwable: Throwable) {
      log.warn("[$corrId] aborting pipeline, cause ${throwable.message}")
      webDocumentDAO.delete(webDocument)
    }
  }

  private fun forwardToMail(corrId: String, webDocument: WebDocumentEntity, subscription: SourceSubscriptionEntity) {
    val mailForwards = mailForwardDAO.findAllBySubscriptionId(subscription.id)
    if (mailForwards.isNotEmpty()) {
      val authorizedMailForwards = mailForwards.filterTo(ArrayList()) { it: MailForwardEntity -> it.authorized }.map { it.email }
      if (authorizedMailForwards.isEmpty()) {
        log.warn("[$corrId] no authorized mail-forwards available of ${mailForwards.size}")
      } else {
        val (mailFormatter, params) = pluginService.resolveMailFormatter(subscription)
        log.info("[$corrId] using formatter ${mailFormatter::class.java.name}")

        val from = mailService.getNoReplyAddress(subscription.product)
        val to = authorizedMailForwards.toTypedArray()

        log.info("[$corrId] resolved mail recipients [${authorizedMailForwards.joinToString(", ")}]")
        mailService.send(
          corrId,
          from,
          to,
          mailFormatter.provideWebDocumentMail(corrId, webDocument, subscription, params)
        )
      }
    }
  }

}
