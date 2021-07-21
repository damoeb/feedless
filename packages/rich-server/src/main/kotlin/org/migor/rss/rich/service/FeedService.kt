package org.migor.rss.rich.service

import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.HttpUtil
import org.migor.rss.rich.api.dto.FeedDto
import org.migor.rss.rich.api.dto.SourceEntryDto
import org.migor.rss.rich.harvest.feedparser.JsonFeedParser
import org.migor.rss.rich.harvest.feedparser.XmlFeedParser
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.database.repository.SourceRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.ConnectException
import java.util.*

@Service
class FeedService {

  private val log = LoggerFactory.getLogger(FeedService::class.simpleName)

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var sourceRepository: SourceRepository

  @Autowired
  lateinit var entryService: EntryService

  @Autowired
  lateinit var propertyService: PropertyService

  @Transactional
  fun updatePubDate(feed: Feed) {
    feedRepository.updatePubDate(feed.id!!, Date())
  }

  @Transactional
  fun updateUpdatedAt(feed: Feed) {
    feedRepository.updateUpdatedAt(feed.id!!, Date())
  }

  fun findBySourceId(sourceId: String): FeedDto {
    val source = sourceRepository.findById(sourceId).orElseThrow { RuntimeException("source $sourceId does not exit") }.toDto()
    val entries = entryService.findLatestBySourceId(sourceId).map { sourceEntry: SourceEntryDto? ->
      run {
        sourceEntry!!.put("comments", "${propertyService.host()}/entry:${sourceEntry.get("id")}/comments")
        sourceEntry
      }
    }
    return FeedDto(null, source.title!!, source.description!!, source.lastUpdatedAt!!, entries, link = "${propertyService.host()}/source:${sourceId}")
  }

  fun parseFeed(url: String): RichFeed {
    log.info("Fetching $url")
    val request = HttpUtil.client.prepareGet(url).execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to $url cause ${e.message}")
    }
    if (response.statusCode != 200) {
      throw HarvestException("Expected 200 received ${response.statusCode}")
    }

    val harvestResponse = HarvestResponse(url, response)
    return when (FeedUtil.simpleContentType(response)) {
      "application/json" -> JsonFeedParser().process(harvestResponse)
      "application/rss+xml", "application/atom+xml", "text/xml", "application/xml" -> XmlFeedParser().process(harvestResponse)
      else -> throw HarvestException("Cannot parse contentType ${response.contentType}")
    }
  }
}
