package org.migor.rss.rich.harvest

import com.github.shyiko.skedule.Schedule
import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Exporter
import org.migor.rss.rich.database.model.ExporterTarget
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.Subscription
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.database.repository.ArticlePostProcessorRepository
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.database.repository.ExporterRepository
import org.migor.rss.rich.database.repository.ExporterTargetRepository
import org.migor.rss.rich.database.repository.SubscriptionRepository
import org.migor.rss.rich.service.ExporterTargetService
import org.migor.rss.rich.service.PipelineService
import org.migor.rss.rich.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
class ExporterHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(ExporterHarvester::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var pipelineService: PipelineService

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var articlePostProcessorRepository: ArticlePostProcessorRepository

  @Autowired
  lateinit var exporterRepository: ExporterRepository

  @Autowired
  lateinit var exporterTargetRepository: ExporterTargetRepository

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

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
      val appendedCount = subscriptionRepository.findAllChangedSince(exporter.id!!)
        .fold(0) { totalArticles, subscription -> totalArticles + exportArticles(corrId, exporter, subscription, targets) }
      val now = Date()
      log.info("[$corrId] Appended $appendedCount articles to bucket ${exporter.bucketId}")
      subscriptionRepository.setLastUpdatedAtByBucketId(exporter.id!!, now)
      exporterRepository.setLastUpdatedAt(exporter.id!!, now)
    } catch (e: Exception) {
      targets.filter { eventTarget -> eventTarget.forwardErrors }.forEach { pushError(it, e) }
      log.error("[$corrId] Cannot update bucket ${exporter.id}: ${e.message}")
    }
  }

  private fun pushError(target: ExporterTarget, exception: Exception) {
    // todo mag push error
  }

  private fun harvestScheduledExporter(exporter: Exporter, targets: List<ExporterTarget>) {
    val corrId = CryptUtil.newCorrId()
    try {
      if (exporter.triggerScheduledNextAt == null) {
        log.info("[$corrId] is unscheduled yet")
        updateScheduledNextAt(corrId, exporter)
      } else {
        val appendedCount = exportArticlesSegment(corrId, exporter, targets)
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
    val scheduledNextAt = Date.from(Schedule.parse(exporter.triggerScheduleExpression).next(ZonedDateTime.now()).toInstant())
    log.info("[$corrId] Next export scheduled for ${scheduledNextAt}")
    exporterRepository.setScheduledNextAt(exporter.id!!, scheduledNextAt)
  }

  private fun exportArticlesSegment(corrId: String, exporter: Exporter, targets: List<ExporterTarget>): Int {
    val subscriptions = subscriptionRepository.findAllByBucketId(exporter.bucketId!!)
    val feedIds = subscriptions.map { subscription -> subscription.feedId!! }.distinct()
    val defaultScheduledLastAt = Date.from(
      LocalDateTime.now().minus(1, ChronoUnit.MONTHS).toInstant(
        ZoneOffset.UTC
      )
    )

    val segmentSize = Optional.ofNullable(exporter.segmentSize).orElse(10)
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
    )
      .map { ArticleSnapshot(it[0] as Article, it[1] as Feed, it[2] as Subscription) }

    return this.exportArticlesToTargets(corrId, articles, exporter, targets)
  }

  private fun exportArticles(
    currId: String,
    exporter: Exporter,
    subscription: Subscription,
    targets: List<ExporterTarget>
  ): Int {
    val articleStream = articleRepository.findNewArticlesForSubscription(subscription.id!!)
      .map { ArticleSnapshot(it[0] as Article, it[1] as Feed, it[2] as Subscription) }

    return this.exportArticlesToTargets(currId, articleStream, exporter, targets)
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
      .map { articleOrNull ->
        run {
          Optional.ofNullable(articleOrNull).ifPresent { pipelineResult ->
            runCatching {
              val article = pipelineResult.first
              this.log.debug("[$corrId] Adding article ${article.article.url} to bucket ${bucket.title}")
              exporterTargetService.pushArticleToTargets(
                corrId,
                article.article,
                bucket.streamId!!,
                bucket.ownerId!!,
                tags = this.mergeTags(pipelineResult.first, pipelineResult.second),
                additionalData = pipelineResult.third,
                pubDate = article.article.pubDate,
                targets = targets
              )
            }
              .onSuccess { log.info("[$corrId] ${bucket.streamId} add article ${pipelineResult.first.article.url}") }
              .onFailure { log.error("[${corrId}] ${it.message}") }
          }
          articleOrNull
        }
      }
      .collect(Collectors.toSet())
      .filterNotNull()
      .count()
  }

  private fun mergeTags(article: ArticleSnapshot, pipelineTags: List<NamespacedTag>): List<NamespacedTag> {
    val tags = ArrayList<NamespacedTag>()
    if (StringUtils.isBlank(article.subscription.name)) {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, article.feed.title!!))
    } else {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, article.subscription.name!!))
    }

    tags.add(NamespacedTag(TagNamespace.FEED_ID, article.feed.id!!))
    tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION_ID, article.subscription.id!!))

    article.subscription.tags?.let { userTags -> tags.addAll(userTags.map { tag -> NamespacedTag(TagNamespace.USER, tag) }) }
    tags.addAll(pipelineTags)

    return tags
  }

  private fun scoreArticle(article: Article, scoreCriteria: String): Pair<Article, Double> {
    return Pair(article, 0.0)
  }
}
