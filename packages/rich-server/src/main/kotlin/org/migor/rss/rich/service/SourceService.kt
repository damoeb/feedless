package org.migor.rss.rich.service

import com.rometools.rome.feed.module.DCModule
import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.dto.FeedDiscovery
import org.migor.rss.rich.dto.SourceDto
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.locate.FeedLocator
import org.migor.rss.rich.model.Source
import org.migor.rss.rich.model.SourceError
import org.migor.rss.rich.model.SourceStatus
import org.migor.rss.rich.repository.SourceErrorRepository
import org.migor.rss.rich.repository.SourceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*

@Service
class SourceService {

  private val log = LoggerFactory.getLogger(SourceService::class.simpleName)

  @Autowired
  lateinit var sourceRepository: SourceRepository

  @Autowired
  lateinit var sourceErrorRepository: SourceErrorRepository

  fun discover(url: String): FeedDiscovery {
    return FeedDiscovery(feeds = FeedLocator.locate(url))
  }

  fun enrichSourceWithFeedDetails(richFeed: RichFeed, source: Source) {
    source.description = StringUtils.trimToNull(richFeed.feed.description)
    source.title = richFeed.feed.title
    source.lang = lang(richFeed.feed.language)
    val dcModule = richFeed.feed.getModule("http://purl.org/dc/elements/1.1/") as DCModule?
    if (dcModule != null && source.lang == null) {
      source.lang = lang(dcModule.language)
    }
    source.siteUrl = richFeed.feed.link

//    if (source.lang == null) {
//      val link = richFeed.feed.link
//      HttpUtil.client.prepareGet(link)
//      source.lang = "en"
//    }

    sourceRepository.save(source)
  }

  private fun lang(language: String?): String? {
    val lang = StringUtils.trimToNull(language)
    return if (lang == null || lang.length < 2) {
      null
    } else {
      lang.substring(0, 2)
    }
  }

  fun updateUpdatedAt(source: Source) {
    sourceRepository.updateUpdatedAt(source.id!!, Date())
  }

  fun findById(sourceId: String): SourceDto {
    return sourceRepository.findById(sourceId).orElseThrow().toDto()
  }

  @Transactional
  fun updateNextHarvestDate(source: Source, hasNewEntries: Boolean) {
    val harvestInterval = if (hasNewEntries) {
      (source.harvestIntervalMinutes * 0.5).toLong().coerceAtLeast(10)
    } else {
      (source.harvestIntervalMinutes * 4).coerceAtMost(700) // twice a day
    }
//  todo mag https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Retry-After
//    val retryAfter = responses.map { response -> response.response.getHeaders("Retry-After") }
//      .filter { retryAfter -> !retryAfter.isEmpty() }
//    slow down fetching if no content, until once a day

    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(harvestInterval, ChronoUnit.MINUTES)))
    log.debug("Scheduling next harvest for source ${source.id} (${source.url}) to $nextHarvestAt")

    sourceRepository.updateNextHarvestAtAndHarvestInterval(source.id!!, nextHarvestAt, harvestInterval)
  }


  @Transactional
  fun updateNextHarvestDateAfterError(source: Source, e: Exception) {
    val nextHarvestAt = Date.from(Date().toInstant().plus(Duration.of(5, ChronoUnit.HOURS)))
    log.info("Rescheduling failed harvest ${source.id}")

    val message = Optional.ofNullable(e.message).orElse(e.javaClass.toString())
    sourceErrorRepository.save(SourceError(message, source))

    val twoWeeksAgo = Date.from(Date().toInstant().minus(Duration.of(2, ChronoUnit.HOURS)))
    sourceErrorRepository.deleteAllBySourceIdAndCreatedAtBefore(source.id!!, twoWeeksAgo)

    if (sourceErrorRepository.countBySourceIdAndCreatedAtAfterOrderByCreatedAtDesc(source.id!!, twoWeeksAgo) >= 5) {
      source.status = SourceStatus.STOPPED
      log.info("Stopping harvest of source ${source.id}")
    }
    source.nextHarvestAt = nextHarvestAt

    sourceRepository.save(source)
  }


}
