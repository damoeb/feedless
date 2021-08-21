package org.migor.rss.rich.cron

import com.rometools.rome.feed.module.DCModule
import com.rometools.rome.feed.synd.SyndContent
import com.rometools.rome.feed.synd.SyndEntry
import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.FeedRepository
import org.migor.rss.rich.harvest.FeedData
import org.migor.rss.rich.harvest.HarvestException
import org.migor.rss.rich.harvest.HarvestResponse
import org.migor.rss.rich.harvest.HarvestUrl
import org.migor.rss.rich.harvest.feedparser.*
import org.migor.rss.rich.harvest.score.ScoreService
import org.migor.rss.rich.service.ArticleService
import org.migor.rss.rich.service.FeedService
import org.migor.rss.rich.service.HttpService
import org.migor.rss.rich.service.StreamService
import org.migor.rss.rich.util.FeedUtil
import org.migor.rss.rich.util.HtmlUtil
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.util.MimeType
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


@Service
class FillFeedCron internal constructor() {

  private val log = LoggerFactory.getLogger(FillFeedCron::class.simpleName)

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var streamService: StreamService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var httpService: HttpService

  private val contentStrategies = arrayOf(
    XmlFeedParser(),
    JsonFeedParser(),
    NullFeedParser()
  )

  private lateinit var feedResolvers: Array<FeedSourceResolver>

  @PostConstruct
  fun onInit() {
    feedResolvers = arrayOf(
      NativeFeedResolver()
    )
  }

  @Scheduled(fixedDelay = 4567)
  fun fetchFeeds() {
    val byHarvestAt = PageRequest.of(0, 10, Sort.by(Sort.Order.asc("nextHarvestAt")))
    val pendingSources = feedRepository.findAllByNextHarvestAtIsBeforeAndStatusEquals(
      Date(), arrayOf(FeedStatus.expired, FeedStatus.stopped), byHarvestAt)

    pendingSources
      .forEach { feed: Feed ->
        try {
          val responses = fetchFeed(feed)
          val feedData = convertToFeeds(responses).filterNotNull()
          if (feedData.isEmpty()) {
            throw RuntimeException("No feeds extracted")
          } else {
            handleFeedData(feed, feedData)
          }

          if (FeedStatus.ok != feed.status) {
            this.log.debug(" status-change for Feed ${feed.feedUrl}: ${feed.status} -> ok")
            feedService.redeemStatus(feed)
          }
        } catch (ex: Exception) {
          log.error("Harvest failed source ${feed.feedUrl}, ${ex.message}")
          if (log.isInfoEnabled) {
            ex.printStackTrace()
          }
          feedService.updateNextHarvestDateAfterError(feed, ex)
        } finally {
          this.log.debug("Finished feed ${feed.feedUrl}")
        }
      }
  }

  fun updateFeedDetails(feedData: FeedData, feed: Feed) {
    feed.description = StringUtils.trimToNull(feedData.feed.description)
    feed.title = feedData.feed.title
    feed.tags = feedData.feed.categories.map { syndCategory -> NamespacedTag(TagNamespace.NONE, syndCategory.name) }.toList()
    feed.lang = lang(feedData.feed.language)
    val dcModule = feedData.feed.getModule("http://purl.org/dc/elements/1.1/") as DCModule?
    if (dcModule != null && feed.lang == null) {
      feed.lang = lang(dcModule.language)
    }
    feed.homePageUrl = feedData.feed.link
    var ftData = arrayOf(feed.title, feed.description, feed.feedUrl, feed.homePageUrl)

    if (!feed.homePageUrl.isNullOrEmpty() && feed.status === FeedStatus.unresolved) {
      try {
        val response = fetchUrl(HarvestUrl(url = feed.homePageUrl!!))
        val doc = Jsoup.parse(response.response.responseBody)
        ftData = ftData.plus(doc.title())
      } catch (e: HarvestException) {
        // ignore
      }
    }
    feed.fulltext = ftData
      .filter { value -> StringUtils.isNotBlank(value) }.joinToString { " " }

    feedRepository.save(feed)
  }

  private fun lang(language: String?): String? {
    val lang = StringUtils.trimToNull(language)
    return if (lang == null || lang.length < 2) {
      null
    } else {
      lang.substring(0, 2)
    }
  }

  private fun handleFeedData(feed: Feed, feedData: List<FeedData>) {
    if (feedData.isNotEmpty()) {
      updateFeedDetails(feedData.first(), feed)

      val articles = feedData.first().feed.entries
              .asSequence()
              .filterNotNull()
        .map { syndEntry -> createArticle(syndEntry, feed) }
        .filterNotNull()
        .filter { article -> !existsArticleByUrl(article.url!!) }
        .map { article -> enrichArticle(article) }
              .toList()

      val newArticlesCount = articles.stream().filter { pair: Pair<Boolean, Article>? -> pair!!.first }.count()
      if (newArticlesCount > 0) {
        log.info("Updating $newArticlesCount articles for ${feed.feedUrl}")
        feedService.updateUpdatedAt(feed)
      } else {
        log.debug("Up-to-date ${feed.feedUrl}")
      }

      articles.map { pair: Pair<Boolean, Article> -> pair.second }
        .forEach { article: Article ->
          run {
            this.log.debug("Adding article ${article.url} to feed ${feed.title}")
            streamService.addArticleToStream(article, feed.streamId!!, "system", emptyList())
          }
        }
      feedService.updateNextHarvestDate(feed, newArticlesCount > 0)
      if (newArticlesCount > 0) {
        this.feedRepository.setLastUpdatedAt(feed.id!!, Date())
      }
    } else {
      feedService.updateNextHarvestDate(feed, false)
    }
  }

  private fun existsArticleByUrl(url: String): Boolean {
    return articleRepository.existsByUrl(url)
  }

  private fun enrichArticle(article: Article): Pair<Boolean, Article> {
    val optionalEntry = articleRepository.findByUrl(article.url!!)
    return if (optionalEntry.isPresent) {
      Pair(false, updateArticleProperties(optionalEntry.get(), article))
    } else {

//      this.mesageQueueService.askForReadability(article)

      Pair(true, scoreService.scoreStatic(article))
    }
  }

  private fun updateArticleProperties(existingArticle: Article, newArticle: Article): Article {
    existingArticle.title = newArticle.title
    existingArticle.content = newArticle.content
    existingArticle.contentHtml = newArticle.contentHtml
    val allTags = HashSet<NamespacedTag>()
    newArticle.tags?.let { tags -> allTags.addAll(tags) }
    existingArticle.tags?.let { tags -> allTags.addAll(tags) }
    existingArticle.tags = allTags.toList()
    return existingArticle
  }

  private fun createArticle(syndEntry: SyndEntry, feed: Feed): Article? {
    return try {
      val article = Article()
      article.url = syndEntry.link
      article.title = syndEntry.title
      val (text, html) = extractContent(syndEntry)
      article.content = text!!
      article.contentHtml = HtmlUtil.cleanHtml(html)
      article.author = getAuthor(syndEntry)
      val tags = syndEntry.categories
        .map { syndCategory -> NamespacedTag(TagNamespace.NONE, syndCategory.name) }
        .toMutableSet()
      if (syndEntry.enclosures != null && syndEntry.enclosures.isNotEmpty()) {
        tags.addAll(syndEntry.enclosures
          .filterNotNull()
          .map { enclusure -> NamespacedTag(TagNamespace.CONTENT, MimeType(enclusure.type).type.toLowerCase()) })
      }
      article.enclosures = JsonUtil.gson.toJson(syndEntry.enclosures)
      article.tags = tags.toList()
      article.commentsFeedUrl = syndEntry.comments
      article.sourceUrl = feed.feedUrl

      article.pubDate = Optional.ofNullable(syndEntry.publishedDate).orElse(Date())
      article.createdAt = Date()
      article
    } catch (e: Exception) {
      null
    }
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

  private fun convertToFeeds(responses: List<HarvestResponse>): List<FeedData?> {
    return responses.map { response ->
      try {
        contentStrategies.first { contentStrategy -> contentStrategy.canProcess(FeedUtil.detectFeedTypeForResponse(response.response)) }
          .process(response)
      } catch (e: Exception) {
        log.error("Failed to convert feed ${e.message}")
        null
      }
    }
  }

  private fun fetchFeed(feed: Feed): List<HarvestResponse> {
    val feedResolver = resolveFeedResolver(feed)
    return feedResolver.feedUrls(feed).stream()
      .map { url -> fetchUrl(url) }
      .collect(Collectors.toList())

  }

  private fun resolveFeedResolver(source: Feed): FeedSourceResolver {
    return feedResolvers.first { feedResolver -> feedResolver.canHandle(source) }
  }

  private fun fetchUrl(url: HarvestUrl): HarvestResponse {
    log.debug("Fetching $url")
    val response = httpService.httpGet(url.url)
    if (response.statusCode != 200) {
      throw HarvestException("Expected 200 received ${response.statusCode}")
    }
    return HarvestResponse(url, response)
  }

}

