package org.migor.rss.rich.service

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
    source.description = richFeed.feed.description
    source.title = richFeed.feed.title
    source.language = richFeed.feed.language
    source.copyright = richFeed.feed.copyright
//    source.pubDate = richFeed.feed.publishedDate
//    feed.source = source
//    feed.subscriptionId = source.id
    sourceRepository.save(source)
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
      (source.harvestIntervalMinutes * 0.5).toLong().coerceAtLeast(2)
    } else {
      (source.harvestIntervalMinutes * 2).coerceAtMost(700) // twice a day
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

    sourceErrorRepository.save(SourceError(e.message!!, source))

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
