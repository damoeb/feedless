package org.migor.rss.rich.scheduler

import org.migor.rss.rich.model.Feed
import org.migor.rss.rich.model.FeedEntry
import org.migor.rss.rich.model.SourceEntry
import org.migor.rss.rich.repository.FeedEntryRepository
import org.migor.rss.rich.repository.FeedRepository
import org.migor.rss.rich.repository.SourceEntryRepository
import org.migor.rss.rich.service.FeedService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
class UpdateDirectFeedEntriesScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(UpdateDirectFeedEntriesScheduler::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var feedEntryRepository: FeedEntryRepository

  @Autowired
  lateinit var sourceEntryRepository: SourceEntryRepository

  @Scheduled(fixedDelay = 6789)
  fun updateFeedEntries() {
    val pageable = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("updatedAt")))

    feedRepository.findAllWhereSourceChanged(pageable)
      .forEach { feed: Feed -> updateFeed(feed) }
  }

  fun updateFeed(feed: Feed) {
    try {
      val entries = sourceEntryRepository.findAllNewEntriesByFeedId(feed.id!!)

      if (entries.isEmpty()) {
        throw RuntimeException()
      } else {
        linkEntriesToFeed(entries, feed)
        feedService.updatePubDate(feed)
      }

    } catch (e: Exception) {
      log.error("Cannot link entries for feed ${feed.id}")
      e.printStackTrace()
    } finally {
      feedService.updateUpdatedAt(feed)
    }
  }

  private fun linkEntriesToFeed(entries: List<SourceEntry>, feed: Feed) {
    entries.forEach { entry: SourceEntry -> feedEntryRepository.save(FeedEntry(entry, feed)) }
    log.info("Linking ${entries.size} entries to feed ${feed.id}")
  }

}

