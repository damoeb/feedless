package org.migor.rich.rss.harvest

import com.github.shyiko.skedule.Schedule
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.database.model.TagNamespace
import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.models.ArticleType
import org.migor.rich.rss.database2.models.BucketEntity
import org.migor.rich.rss.database2.models.ExporterEntity
import org.migor.rich.rss.database2.models.ExporterTargetEntity
import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.models.SubscriptionEntity
import org.migor.rich.rss.database2.repositories.ArticleDAO
import org.migor.rich.rss.database2.repositories.BucketDAO
import org.migor.rich.rss.database2.repositories.ExporterDAO
import org.migor.rich.rss.database2.repositories.ExporterTargetDAO
import org.migor.rich.rss.database2.repositories.RefinementDAO
import org.migor.rich.rss.database2.repositories.SubscriptionDAO
import org.migor.rich.rss.database2.repositories.UserDAO
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
@Profile("database2")
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
  lateinit var refinementDAO: RefinementDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var subscriptionDAO: SubscriptionDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var userRepository: UserDAO

  @Autowired
  lateinit var exporterDAO: ExporterDAO

  @Autowired
  lateinit var exporterTargetDAO: ExporterTargetDAO

  fun harvestExporter(exporter: ExporterEntity) {
    log.info("harvestExporter ${exporter.id}")
    val targets = exporterTargetDAO.findAllByExporterId(exporter.id)
    if ("change" == exporter.triggerRefreshOn) {
      this.harvestOnChangeExporter(exporter, targets)
    }
    if ("scheduled" == exporter.triggerRefreshOn) {
      this.harvestScheduledExporter(exporter, targets)
    }
  }

  private fun harvestOnChangeExporter(exporter: ExporterEntity, targets: List<ExporterTargetEntity>) {
    val corrId = CryptUtil.newCorrId()
    try {
      log.info("[$corrId] harvestOnChangeExporter exporter ${exporter.id}")
      val appendedCount = subscriptionDAO.findAllByExporterId(exporter.id)
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
        log.info("[$corrId] Appended $appendedCount articles to stream ${propertyService.publicUrl}/bucket:${exporter.bucketId}")
      }
      val now = Date()
      log.info("[$corrId] Updating lastUpdatedAt for subscription and exporter")
      subscriptionDAO.setLastUpdatedAtByBucketId(exporter.id, now)
      exporterDAO.setLastUpdatedAt(exporter.id, now)
    } catch (e: Exception) {
      e.printStackTrace()
      log.error("[$corrId] Cannot run exporter ${exporter.id} for bucket ${exporter.bucketId}: ${e.message}")
    }
  }

  private fun harvestScheduledExporter(exporter: ExporterEntity, targets: List<ExporterTargetEntity>) {
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
        exporterDAO.setLastUpdatedAt(exporter.id, now)

        subscriptionDAO.setLastUpdatedAtByBucketId(exporter.id, now)
      }

    } catch (e: Exception) {
      log.error("[$corrId] Cannot update scheduled bucket ${exporter.id}: ${e.message}")
    }
  }

  private fun updateScheduledNextAt(corrId: String, exporter: ExporterEntity) {
    val scheduledNextAt =
      Date.from(Schedule.parse(exporter.triggerScheduleExpression).next(ZonedDateTime.now()).toInstant())
    log.info("[$corrId] Next export scheduled for $scheduledNextAt")
    exporterDAO.setScheduledNextAt(exporter.id, scheduledNextAt)
  }

  private fun scheduledExportArticles(corrId: String, exporter: ExporterEntity, targets: List<ExporterTargetEntity>): Int {
    val subscriptions = subscriptionDAO.findAllByBucketId(exporter.bucketId!!)
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
    val articles = articleDAO.findAllThrottled(
      feedIds,
      Optional.ofNullable(exporter.triggerScheduledLastAt).orElse(defaultScheduledLastAt),
      pageable
    ).map { ArticleSnapshot(it[0] as ArticleEntity, it[1] as NativeFeedEntity, it[2] as SubscriptionEntity) }

    return scheduledExportToTargets(corrId, articles, exporter, targets)
  }

  private fun exportArticles(
    corrId: String,
    exporter: ExporterEntity,
    subscription: SubscriptionEntity,
    targets: List<ExporterTargetEntity>
  ): Int {
    val articleStream = if (exporter.lookAheadMin == null) {
      articleDAO.findNewArticlesForSubscription(subscription.id)
    } else {
      log.info("[${corrId}] with look-ahead ${exporter.lookAheadMin}")
      articleDAO.findArticlesForSubscriptionWithLookAhead(subscription.id, exporter.lookAheadMin!!)
    }.map { ArticleSnapshot(it[0] as ArticleEntity, it[1] as NativeFeedEntity, it[2] as SubscriptionEntity) }

    return this.exportArticlesToTargets(corrId, articleStream, exporter, targets)
  }

  private fun scheduledExportToTargets(
    corrId: String,
    articles: Stream<ArticleSnapshot>,
    exporter: ExporterEntity,
    targets: List<ExporterTargetEntity>
  ): Int {
    val bucket = bucketDAO.findById(exporter.bucketId!!).orElseThrow()

    val refinement = refinementDAO.findAllByBucketId(exporter.bucketId!!)

    val listOfArticles = articles
      .map { pipelineService.triggerPipeline(corrId, refinement, it, bucket) }
      .collect(Collectors.toSet())
      .filterNotNull()

    if (listOfArticles.isEmpty()) {
      return 0
    }

    // todo mag
    if (exporter.digest) {
      this.log.info("[${corrId}] digest")

      val user = bucket.owner!!
      val dateFormat = Optional.ofNullable(user.dateFormat).orElse(propertyService.dateFormat)
      val digest = articleService.save(
        createDigestOfArticles(
          bucket.name!!,
          dateFormat,
          listOfArticles.map { (snapshot, _, _) -> snapshot.article })
      )

      exporterTargetService.pushArticlesToTargets(
        corrId,
        listOf(digest),
        bucket.stream!!,
        ArticleType.digest,
        bucket.owner!!,
        Date(),
//        tags = listOf(NamespacedTag(TagNamespace.USER, "digest")),
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
      articles: List<ArticleEntity>
    ): ArticleEntity {
      val digest = ArticleEntity()
      val listOfAttributes = articles.map { article ->
        mapOf(
          "title" to article.title,
          "host" to URL(article.url).host,
          "pubDate" to formatDateForUser(
            article.publishedAt!!,
            dateFormat
          ),
          "url" to article.url,
          "description" to toTextEllipsis(Optional.ofNullable(article.contentText).orElse(""))
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

      digest.publishedAt = Date()
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
    exporter: ExporterEntity,
    targets: List<ExporterTargetEntity>
  ): Int {
    val bucket = bucketDAO.findById(exporter.bucketId!!).orElseThrow()

    val postProcessors = refinementDAO.findAllByBucketId(exporter.bucketId!!)

    return articles
      .map { pipelineService.triggerPipeline(corrId, postProcessors, it, bucket) }
      .collect(Collectors.toSet())
      .filterNotNull()
      .also { snapshots -> pushToTargets(corrId, bucket, targets, snapshots) }
      .count()
  }

  private fun pushToTargets(
    corrId: String,
    bucket: BucketEntity,
    targets: List<ExporterTargetEntity>,
    snapshots: List<Triple<ArticleSnapshot, List<NamespacedTag>, Map<String, String>>>
  ) {
    snapshots.forEach { data ->
      runCatching {
        val (article, tags, additionalData) = data
        val pubDate = if (article.article.publishedAt!! > Date()) {
          article.article.publishedAt!!
        } else {
          log.debug("[${corrId}] Overwriting pubDate cause is in past")
          Date()
        }
        exporterTargetService.pushArticlesToTargets(
          corrId,
          listOf(article.article),
          bucket.stream!!,
          ArticleType.feed,
          bucket.owner!!,
//          tags = this.mergeTags(article, tags),
//          additionalData = additionalData,
          overwritePubDate = pubDate,
          targets = targets
        )
      }.onFailure { log.error("[${corrId}] pushArticleToTargets failed: ${it.message}") }
    }
    log.debug("[${corrId}] Updated bucket-feed ${propertyService.publicUrl}/bucket:${bucket.id}")
  }

  private fun mergeTags(article: ArticleSnapshot, pipelineTags: List<NamespacedTag>): List<NamespacedTag> {
    val tags = ArrayList<NamespacedTag>()
    // todo tags should be dynamic and attached to articleRef not article
    val subscriptionName = article.subscription.feed!!.title!!
    if (StringUtils.isBlank(subscriptionName)) {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, article.feed.title!!))
    } else {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, subscriptionName))
    }

//    todo mag tags
//    article.subscription.tags?.let { userTags ->
//      tags.addAll(userTags.map { tag ->
//        NamespacedTag(
//          TagNamespace.USER,
//          tag
//        )
//      })
//    }
    tags.addAll(pipelineTags)

    return tags
  }
}
