package org.migor.rich.rss.harvest

import com.github.shyiko.skedule.Schedule
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.database.model.Article
import org.migor.rich.rss.database.model.ArticleRefType
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.Exporter
import org.migor.rich.rss.database.model.ExporterTarget
import org.migor.rich.rss.database.model.Feed
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.database.model.Subscription
import org.migor.rich.rss.database.model.TagNamespace
import org.migor.rich.rss.database.repository.ArticlePostProcessorRepository
import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.database.repository.BucketRepository
import org.migor.rich.rss.database.repository.ExporterRepository
import org.migor.rich.rss.database.repository.ExporterTargetRepository
import org.migor.rich.rss.database.repository.SubscriptionRepository
import org.migor.rich.rss.database.repository.UserRepository
import org.migor.rich.rss.pipeline.PipelineService
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.ExporterTargetService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
@Profile("stateful")
class ExporterHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(ExporterHarvester::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var pipelineService: PipelineService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @Autowired
  lateinit var articlePostProcessorRepository: ArticlePostProcessorRepository

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  lateinit var exporterRepository: ExporterRepository

  @Autowired
  lateinit var exporterTargetRepository: ExporterTargetRepository

  fun harvestExporter(exporter: Exporter) {
    val targets = exporterTargetRepository.findAllByExporterId(exporter.id!!)
    if ("change" == exporter.triggerRefreshOn) {
      this.harvestOnChangeExporter(exporter, targets)
    }
    if ("scheduled" == exporter.triggerRefreshOn) {
      this.harvestScheduledExporter(exporter, targets)
    }
  }

  private fun harvestOnChangeExporter(exporter: Exporter, targets: List<ExporterTarget>) {
    val corrId = CryptUtil.newCorrId()
    try {
      log.info("[$corrId] harvestOnChangeExporter exporter ${exporter.id}")
      val appendedCount = subscriptionRepository.findAllByExporterId(exporter.id!!)
        .fold(0) { totalArticles, subscription ->
          run {
            log.info("[${corrId}] subscription ${subscription.id} is outdated")
            totalArticles + exportArticles(
              corrId,
              exporter,
              subscription,
              targets
            )
          }
        }
      if (appendedCount > 0) {
        log.info("[$corrId] Appended $appendedCount articles to stream ${propertyService.host}/bucket:${exporter.bucketId}")
      }
      val now = Date()
      log.info("[$corrId] Updating lastUpdatedAt for subscription and exporter")
      subscriptionRepository.setLastUpdatedAtByBucketId(exporter.id!!, now)
      exporterRepository.setLastUpdatedAt(exporter.id!!, now)
    } catch (e: Exception) {
      log.error("[$corrId] Cannot run exporter ${exporter.id} for bucket ${exporter.bucketId}: ${e.message}")
    }
  }

  private fun harvestScheduledExporter(exporter: Exporter, targets: List<ExporterTarget>) {
    val corrId = CryptUtil.newCorrId()
    log.info("[$corrId] harvestScheduledExporter exporter ${exporter.id}")
    try {
      if (exporter.triggerScheduledNextAt == null) {
        log.info("[$corrId] is unscheduled yet")
        updateScheduledNextAt(corrId, exporter)
      } else {
        val appendedCount = scheduledExportArticles(corrId, exporter, targets)
        val now = Date()
        log.info("[$corrId] Appended segment of size $appendedCount to bucket ${exporter.bucketId}")

        updateScheduledNextAt(corrId, exporter)
        exporterRepository.setLastUpdatedAt(exporter.id!!, now)

        subscriptionRepository.setLastUpdatedAtByBucketId(exporter.id!!, now)
      }

    } catch (e: Exception) {
      log.error("[$corrId] Cannot update scheduled bucket ${exporter.id}: ${e.message}")
    }
  }

  private fun updateScheduledNextAt(corrId: String, exporter: Exporter) {
    val scheduledNextAt =
      Date.from(Schedule.parse(exporter.triggerScheduleExpression).next(ZonedDateTime.now()).toInstant())
    log.info("[$corrId] Next export scheduled for $scheduledNextAt")
    exporterRepository.setScheduledNextAt(exporter.id!!, scheduledNextAt)
  }

  private fun scheduledExportArticles(corrId: String, exporter: Exporter, targets: List<ExporterTarget>): Int {
    val subscriptions = subscriptionRepository.findAllByBucketId(exporter.bucketId!!)
    val feedIds = subscriptions.map { subscription -> subscription.feedId!! }.distinct()
    val defaultScheduledLastAt = Date.from(
      LocalDateTime.now().minus(1, ChronoUnit.MONTHS).toInstant(
        ZoneOffset.UTC
      )
    )

    val segmentSize = Optional.ofNullable(exporter.segmentSize).orElse(100)
    val segmentSortField = Optional.ofNullable(exporter.segmentSortField).orElse("score")
    val segmentSortOrder = if (exporter.segmentSortAsc) {
      Sort.Order.asc(segmentSortField)
    } else {
      Sort.Order.desc(segmentSortField)
    }
    val pageable = PageRequest.of(0, segmentSize, Sort.by(segmentSortOrder))
    val articles = articleRepository.findAllThrottled(
      feedIds,
      Optional.ofNullable(exporter.triggerScheduledLastAt).orElse(defaultScheduledLastAt),
      pageable
    ).map { ArticleSnapshot(it[0] as Article, it[1] as Feed, it[2] as Subscription) }

    return scheduledExportToTargets(corrId, articles, exporter, targets)
  }

  private fun exportArticles(
    corrId: String,
    exporter: Exporter,
    subscription: Subscription,
    targets: List<ExporterTarget>
  ): Int {
    val articleStream = if (exporter.lookAheadMin == null) {
      articleRepository.findNewArticlesForSubscription(subscription.id!!)
    } else {
      log.info("[${corrId}] with look-ahead ${exporter.lookAheadMin}")
      articleRepository.findArticlesForSubscriptionWithLookAhead(subscription.id!!, exporter.lookAheadMin!!)
    }.map { ArticleSnapshot(it[0] as Article, it[1] as Feed, it[2] as Subscription) }

    return this.exportArticlesToTargets(corrId, articleStream, exporter, targets)
  }

  private fun scheduledExportToTargets(
    corrId: String,
    articles: Stream<ArticleSnapshot>,
    exporter: Exporter,
    targets: List<ExporterTarget>
  ): Int {
    val bucket = bucketRepository.findById(exporter.bucketId!!).orElseThrow()

    val postProcessors = articlePostProcessorRepository.findAllByBucketId(exporter.bucketId!!)

    val listOfArticles = articles
      .map { pipelineService.triggerPipeline(corrId, postProcessors, it, bucket) }
      .collect(Collectors.toSet())
      .filterNotNull()

    if (listOfArticles.isEmpty()) {
      return 0
    }

    if (exporter.digest) {
      this.log.info("[${corrId}] digest")

      val user = userRepository.findById(bucket.ownerId).orElseThrow()
      val dateFormat = Optional.ofNullable(user.dateFormat).orElse(propertyService.dateFormat)
      val digest = articleService.save(
        createDigestOfArticles(
          bucket.name,
          dateFormat,
          listOfArticles.map { (snapshot, _, _) -> snapshot.article })
      )

      exporterTargetService.pushArticleToTargets(
        corrId,
        digest,
        bucket.streamId,
        ArticleRefType.digest,
        bucket.ownerId,
        tags = listOf(NamespacedTag(TagNamespace.USER, "digest")),
        pubDate = Date(),
        targets = targets
      )
    } else {
      pushToTargets(corrId, bucket, targets, listOfArticles)
    }
    return listOfArticles.size
  }

  companion object {
    fun createDigestOfArticles(
      bucketName: String,
      dateFormat: String,
      articles: List<Article>
    ): Article {
      val digest = Article()
      val listOfAttributes = articles.map { article ->
        mapOf(
          "title" to article.title,
          "host" to URL(article.url).host,
          "pubDate" to formatDateForUser(
            article.pubDate,
            dateFormat
          ),
          "url" to article.url,
          "description" to toTextEllipsis(article.contentText)
        )
      }

      digest.contentRaw = "<ul>${
        listOfAttributes.joinToString("\n") { attributes ->
          """<li>
<a href="${attributes["url"]}">${attributes["pubDate"]} ${attributes["title"]} (${attributes["host"]})</a>
<p>${attributes["description"]}</p>
</li>""".trimMargin()
        }
      }</ul>"
      digest.contentRawMime = "text/html"
      digest.contentText = listOfAttributes.joinToString("\n\n") { attributes ->
        """- ${attributes["pubDate"]} ${attributes["title"]}
  [${attributes["url"]}]

  ${attributes["description"]}"""
      }

      digest.pubDate = Date()
      digest.title = "${bucketName.uppercase()} Digest ${
        formatDateForUser(
          Date(),
          dateFormat
        )
      }"

      return digest
    }

    private fun toTextEllipsis(contentText: String): String {
      return if (contentText.length > 100) {
        contentText.subSequence(0, 100).toString() + "..."
      } else {
        contentText
      }
    }

    private fun formatDateForUser(date: Date, dateTimeFormat: String): String {
      return SimpleDateFormat(dateTimeFormat).format(date)
    }
  }

  private fun exportArticlesToTargets(
    corrId: String,
    articles: Stream<ArticleSnapshot>,
    exporter: Exporter,
    targets: List<ExporterTarget>
  ): Int {
    val bucket = bucketRepository.findById(exporter.bucketId!!).orElseThrow()

    val postProcessors = articlePostProcessorRepository.findAllByBucketId(exporter.bucketId!!)

    return articles
      .map { pipelineService.triggerPipeline(corrId, postProcessors, it, bucket) }
      .collect(Collectors.toSet())
      .filterNotNull()
      .also { snapshots -> pushToTargets(corrId, bucket, targets, snapshots) }
      .count()


  }

  private fun pushToTargets(
    corrId: String,
    bucket: Bucket,
    targets: List<ExporterTarget>,
    snapshots: List<Triple<ArticleSnapshot, List<NamespacedTag>, Map<String, String>>>
  ) {
    snapshots.forEach { data ->
      runCatching {
        val (article, tags, additionalData) = data
        val pubDate = if (article.article.pubDate > Date()) {
          article.article.pubDate
        } else {
          log.debug("[${corrId}] Overwriting pubDate cause is in past")
          Date()
        }
        exporterTargetService.pushArticleToTargets(
          corrId,
          article.article,
          bucket.streamId,
          ArticleRefType.feed,
          bucket.ownerId,
          tags = this.mergeTags(article, tags),
          additionalData = additionalData,
          pubDate = pubDate,
          targets = targets
        )
      }.onFailure { log.error("[${corrId}] pushArticleToTargets failed: ${it.message}") }
    }
    log.debug("[${corrId}] Updated bucket-feed ${propertyService.host}/bucket:${bucket.id}")
  }

  private fun mergeTags(article: ArticleSnapshot, pipelineTags: List<NamespacedTag>): List<NamespacedTag> {
    val tags = ArrayList<NamespacedTag>()
    // todo tags should be dynamic and attached to articleRef not article
    if (StringUtils.isBlank(article.subscription.name)) {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, article.feed.title!!))
    } else {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, article.subscription.name!!))
    }

    article.subscription.tags?.let { userTags ->
      tags.addAll(userTags.map { tag ->
        NamespacedTag(
          TagNamespace.USER,
          tag
        )
      })
    }
    tags.addAll(pipelineTags)

    return tags
  }
}
