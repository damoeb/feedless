package org.migor.rss.rich.cron

import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.HtmlUtil
import org.migor.rss.rich.util.HttpUtil
import org.migor.rss.rich.harvest.feedparser.*
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.harvest.RichFeed
import org.migor.rss.rich.database.model.*
import org.migor.rss.rich.database.repository.SourceEntryRepository
import org.migor.rss.rich.database.repository.SourceRepository
import org.migor.rss.rich.service.PropertyService
import org.migor.rss.rich.service.SourceService
import org.migor.rss.rich.transform.BaseTransform
import org.migor.rss.rich.transform.EntryTransform
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigInteger
import java.net.ConnectException
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.annotation.PostConstruct
import kotlin.collections.HashMap


@Component
class HarvestSourcesScheduler internal constructor() {

  private val log = LoggerFactory.getLogger(HarvestSourcesScheduler::class.simpleName)

  @Autowired
  lateinit var sourceService: SourceService

  @Autowired
  lateinit var entryRepository: SourceEntryRepository

  @Autowired
  lateinit var sourceRepository: SourceRepository

  @Autowired
  lateinit var propertyService: PropertyService

  private val contentStrategies = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  private lateinit var feedResolvers: Array<FeedSourceResolver>
  private lateinit var entryPostProcessors: Array<EntryTransform>

  @PostConstruct
  fun onInit() {
    val twitterSupport = TwitterFeedSupport(propertyService)
    feedResolvers = arrayOf(
      twitterSupport,
      NativeFeedResolver()
    )

    entryPostProcessors = arrayOf(
      twitterSupport,
      BaseTransform()
    )
  }

  @Scheduled(fixedDelay = 13456)
  fun harvestPending() {
    val byCreatedAt = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdAt")))
    val newSources = sourceRepository.findAllByNextHarvestAtIsNullAndStatus(SourceStatus.FINE, byCreatedAt)

    val byHarvestAt = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextHarvestAt")))
    // todo mag only harvest sources that have subscriptions?
    val pendingSources = sourceRepository.findAllByNextHarvestAtBeforeAndStatus(Date(), SourceStatus.FINE, byHarvestAt)

    Stream.concat(newSources.stream(), pendingSources.stream())
      .forEach { source: Source ->
        try {
          val responses = fetchSource(source)
          val feeds = convertToFeeds(responses).filterNotNull()
          if (feeds.isEmpty()) {
            throw RuntimeException("zero feeds extracted")
          } else {
            postProcessEntries(source, feeds)
          }

          if (SourceStatus.FINE != source.status) {
            sourceService.redeemStatus(source)
          }

        } catch (ex: Exception) {
          log.error("Harvest failed source ${source.id} (${source.url}), ${ex.message}")
//          if (log.isDebugEnabled) {
//            e.printStackTrace()
//          }
          sourceService.updateNextHarvestDateAfterError(source, ex)
        }
      }
  }

  private fun postProcessEntries(source: Source, feeds: List<RichFeed>) {
    if (feeds.isNotEmpty()) {
      sourceService.enrichSourceWithFeedDetails(feeds.first(), source)

      val entryPostProcessor = resolveEntryPostProcessor(source)
      val entries = feeds.first().feed.entries
        .map { syndEntry -> createSourceEntry(syndEntry, source) }
        .filter { entry -> !existsEntryByLink(entry.first.link!!) }
        .map { entry -> entryPostProcessor.applyTransform(source, entry.first, entry.second, feeds) }
        .map { entry -> finalizeEntry(entry) }
        .map { entry -> updateEntry(entry) }

      val newEntriesCount = entries.stream().filter { pair: Pair<Boolean, SourceEntry>? -> pair!!.first }.count()
      if (newEntriesCount > 0) {
        log.info("Updating $newEntriesCount entries for ${source.url}")
        sourceService.updateUpdatedAt(source)
      } else {
        log.info("Up-to-date ${source.url}")
      }

      entries.map { pair: Pair<Boolean, SourceEntry> -> pair.second }
        .forEach { sourceEntry: SourceEntry ->
          run {
            try {
              entryRepository.save(sourceEntry)
            } catch (e: DataIntegrityViolationException) {
            }
          }
        }
      sourceService.updateNextHarvestDate(source, newEntriesCount > 0)
    }
  }

  private fun calculateEntryRate(source: Source) {
    val byCreatedAt = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("createdAt")))
    val entries = entryRepository.findLatestCreatedAtBySourceId(source.id, byCreatedAt)
    val lastCreatedAt = entries.last().createdAt.toInstant()

    val period: Period = Period.between(LocalDate.now(), LocalDate.ofInstant(lastCreatedAt, ZoneId.systemDefault()))

    val unit = if (period.years > 0) {
      "yearly"
    } else if (period.months > 0) {
      "monthly"
    } else "daily"

    log.info("avg production rate is $unit")
  }

  private fun existsEntryByLink(url: String): Boolean {
    return entryRepository.existsByLink(url)
  }

  private fun finalizeEntry(entry: SourceEntry): SourceEntry {
    entry.status = EntryStatus.RELEASED
    return entry
  }

  private fun updateEntry(entry: SourceEntry): Pair<Boolean, SourceEntry> {
    val optionalEntry = entryRepository.findByLink(entry.link)
    return if (optionalEntry.isPresent) {
      Pair(false, mergeEntries(optionalEntry.get(), entry))
    } else {
      Pair(true, entry)
    }
  }

  private fun mergeEntries(existingEntry: SourceEntry, entry: SourceEntry): SourceEntry {
    val merged = HashMap<String, Any>(100)

    existingEntry.properties?.let { existingContent ->
      existingContent.keys
        .forEach { key -> merged[key] = existingContent.getValue(key) }
    }
    entry.properties?.let { content ->
      content.keys
        .filter { key -> content.getValue(key) is BigInteger }
        .forEach { numericKey -> merged[numericKey] = content.getValue(numericKey) }
    }

    existingEntry.properties = merged
    return existingEntry
  }

  private fun createSourceEntry(syndEntry: SyndEntry, source: Source): Pair<SourceEntry, SyndEntry> {
    val entry = SourceEntry()
    entry.source = source
    entry.link = syndEntry.link
    entry.title = syndEntry.title
    val (text, html) = extractContent(syndEntry)
    entry.content = text
    val hasHtml = StringUtils.isNoneBlank(html)
    entry.hasContentHtml = hasHtml
    if (hasHtml) {
      entry.contentHtml = cleanHtml(html!!)
    }
    entry.author = getAuthor(syndEntry)

    val properties: Map<String, Any> = mapOf(
      "link" to syndEntry.link,
      "comments" to syndEntry.comments,
      "wordCount" to Arrays.stream(StringUtils.split(text, " ,;:'.!?"))
        .filter(StringUtils::isNotBlank)
        .count().toInt(),
      "categories" to emptyToNull(syndEntry.categories),
      "contributors" to emptyToNull(syndEntry.contributors),
      "updatedDate" to syndEntry.updatedDate,
      "enclosures" to emptyToNull(syndEntry.enclosures),
      "publishedDate" to syndEntry.publishedDate,
    ).filterValues { it != null }
      .mapValues { it.value as Any }

    if (syndEntry.publishedDate == null) {
      entry.pubDate = Date()
    } else {
      entry.pubDate = syndEntry.publishedDate
    }
    entry.properties = properties
    entry.createdAt = Date()
    return Pair(entry, syndEntry)
  }

  private fun cleanHtml(html: String): String {
    return Jsoup.clean(html, Whitelist.basicWithImages())
  }

  private fun getAuthor(syndEntry: SyndEntry) =
    Optional.ofNullable(StringUtils.trimToNull(syndEntry.author)).orElse("unknown")

  private fun extractContent(syndEntry: SyndEntry): Pair<String?, String?> {
    val contents = ArrayList<SyndContent>()
    contents.addAll(syndEntry.contents)
    if (syndEntry.description != null) {
      contents.add(syndEntry.description)
    }
    val html = contents.find { syndContent -> syndContent.type != null && syndContent.type.toLowerCase().endsWith("html") }?.value
    val text = if (contents.isNotEmpty()) {
      if (html == null) {
        contents.first().value
      } else {
        HtmlUtil.html2text(html)
      }
    } else {
      null
    }
    return Pair(text, html)
  }

  private fun emptyToNull(list: List<Any>): Any? {
    return if (list.isEmpty()) {
      null
    } else {
      list
    }
  }

  private fun convertToFeeds(responses: List<HarvestResponse>): List<RichFeed?> {
    return responses.map { response ->
      try {
        contentStrategies.first { contentStrategy -> contentStrategy.canProcess(FeedUtil.detectFeedType(response.response)) }.process(response)
      } catch (e: Exception) {
        log.error("Failed to convert feed ${e.message}")
        null
      }
    }
  }

  private fun fetchSource(source: Source): List<HarvestResponse> {
    val feedResolver = resolveFeedResolver(source)
    return feedResolver.feedUrls(source).stream()
      .map { url -> fetchUrl(url) }
      .collect(Collectors.toList())

  }

  private fun fetchUrl(url: HarvestUrl): HarvestResponse {
    log.info("Fetching $url")
    val request = HttpUtil.client.prepareGet(url.url).execute()

    val response = try {
      request.get()
    } catch (e: ConnectException) {
      throw HarvestException("Cannot connect to ${url.url} cause ${e.message}")
    }
    if (response.statusCode != 200) {
      throw HarvestException("Expected 200 received ${response.statusCode}")
    }
    return HarvestResponse(url, response)
  }

  private fun resolveEntryPostProcessor(source: Source): EntryTransform {
    return entryPostProcessors.first { postProcessor -> postProcessor.canHandle(source) }
  }

  private fun resolveFeedResolver(source: Source): FeedSourceResolver {
    return feedResolvers.first { feedResolver -> feedResolver.canHandle(source) }
  }
}

