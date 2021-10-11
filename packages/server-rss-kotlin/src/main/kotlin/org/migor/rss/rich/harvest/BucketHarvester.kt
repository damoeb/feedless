package org.migor.rss.rich.harvest

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.database.model.*
import org.migor.rss.rich.database.repository.*
import org.migor.rss.rich.harvest.entryfilter.generated.TakeEntryIfRunner
import org.migor.rss.rich.service.StreamService
import org.migor.rss.rich.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*


@Service
class BucketHarvester internal constructor() {

  private val log = LoggerFactory.getLogger(BucketHarvester::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var feedRepository: FeedRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var articlePostProcessorRepository: ArticlePostProcessorRepository

  @Autowired
  lateinit var streamService: StreamService

  fun harvestBucket(bucket: Bucket) {
    if ("change" == bucket.triggerRefreshOn) {
      this.harvestOnChangeBucket(bucket)
    }
    if ("scheduled" == bucket.triggerRefreshOn) {
      this.harvestScheduledBucket(bucket)
    }
  }

  private fun harvestOnChangeBucket(bucket: Bucket) {
    val cid = CryptUtil.newCorrId()
    // find subscriptions that changed since last bucket change
    try {
      subscriptionRepository.findAllChangedSince(bucket.id!!)
        .forEach { subscription -> suggestArticles(cid, bucket, subscription) }
      val now = Date()
      log.info("[${cid}] Updating lastUpdatedAt for bucket ${bucket.id} and related subscription")
      subscriptionRepository.setLastUpdatedAtByBucketId(bucket.id!!, now)
      bucketRepository.setLastUpdatedAt(bucket.id!!, now)

    } catch (e: Exception) {
      log.error("[${cid}] Cannot update bucket ${bucket.id}: ${e.message}")
    }
  }

  private fun harvestScheduledBucket(bucket: Bucket) {
    val cid = CryptUtil.newCorrId()
    try {
      suggestArticlesThrottled(cid, bucket)
      val now = Date()
      log.info("[${cid}] Updating lastUpdatedAt for bucket ${bucket.id} and related subscription")
      subscriptionRepository.setLastUpdatedAtByBucketId(bucket.id!!, now)
      bucketRepository.setLastUpdatedAt(bucket.id!!, now)

    } catch (e: Exception) {
      log.error("[${cid}] Cannot update scheduled bucket ${bucket.id}: ${e.message}")
    }
  }

  private fun suggestArticlesThrottled(cid: String, bucket: Bucket) {
    val subscriptions = subscriptionRepository.findAllByBucketId(bucket.id!!)
    val feedIds = subscriptions.map { subscription -> subscription.feedId!! }.distinct()
    val defaultScheduledLastAt = Date.from(
      LocalDateTime.now().minus(1, ChronoUnit.MONTHS).toInstant(
        ZoneOffset.UTC
      )
    )
    articleRepository.findAllThrottled(
      feedIds,
      Optional.ofNullable(bucket.triggerScheduledLastAt).orElse(defaultScheduledLastAt)
    )
      .filter { article -> this.filterArticle(cid, bucket, article)}
      .forEach { article ->
      run {
        this.log.debug("[${cid}] Adding article ${article.url} to bucket ${bucket.title}")
        streamService.addArticleToStream(
          cid,
          article,
          bucket.streamId!!,
          bucket.ownerId!!,
          tags = emptyList(), // this.getTags(feed, subscription),
          pubDate = Date()
        )
      }
    }
  }

  private fun suggestArticles(cid: String, bucket: Bucket, subscription: Subscription): Subscription {
    val feed = feedRepository.findById(subscription.feedId!!).orElseThrow()

    articleRepository.findNewArticlesForSubscription(subscription.id!!)
      .filter { article -> this.filterArticle(cid, bucket, article)}
      .forEach { article ->
        run {
          this.log.debug("[${cid}] Adding article ${article.url} to bucket ${bucket.title}")
          streamService.addArticleToStream(
            cid,
            article,
            bucket.streamId!!,
            bucket.ownerId!!,
            tags = this.getTags(feed, subscription),
            article.pubDate
          )
        }
      }

    return subscription
  }

  private fun filterArticle(cid: String, bucket: Bucket, article: Article): Boolean {
    val filterExecutorOpt = Optional.ofNullable(createTakeIfRunner(cid, bucket.filterExpression))
    return if (filterExecutorOpt.isPresent) {
      val matches = executeFilter(cid, bucket.filterExpression!!, article)
      if (!matches) {
        log.info("[${cid}] Dropping article ${article.url}")
      }
      matches
    } else {
      true
    }
  }

  private fun getTags(feed: Feed, subscription: Subscription): List<NamespacedTag> {
    val tags = ArrayList<NamespacedTag>()
    if (StringUtils.isBlank(subscription.name)) {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, feed.title!!))
    } else {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, subscription.name!!))
    }

    tags.add(NamespacedTag(TagNamespace.FEED_ID, feed.id!!))
    tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION_ID, subscription.id!!))

    subscription.tags?.let { userTags -> tags.addAll(userTags.map { tag -> NamespacedTag(TagNamespace.USER, tag) }) }

    return tags;
  }

  private fun executeFilter(cid: String, filterExecutor: String, article: Article): Boolean {
    return createTakeIfRunner(cid, filterExecutor)!!.takeIf(article)
  }

  private fun createTakeIfRunner(cid: String, filterExpression: String?): TakeEntryIfRunner? {
    return try {
      filterExpression?.let { expr -> TakeEntryIfRunner(expr.byteInputStream()) }
    } catch (e: Exception) {
      log.error("[${cid}] Invalid filter expression ${filterExpression}, ${e.message}")
      null
    }
  }

  private fun scoreArticle(article: Article, scoreCriteria: String): Pair<Article, Double> {
    return Pair(article, 0.0)
  }
}

