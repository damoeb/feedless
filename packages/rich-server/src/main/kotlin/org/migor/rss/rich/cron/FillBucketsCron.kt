package org.migor.rss.rich.cron

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.ReleaseThrottle
import org.migor.rss.rich.database.model.Subscription
import org.migor.rss.rich.database.repository.*
import org.migor.rss.rich.harvest.entryfilter.generated.TakeEntryIfRunner
import org.migor.rss.rich.service.StreamService
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*


@Component
class FillBucketsCron internal constructor() {

  private val log = LoggerFactory.getLogger(FillBucketsCron::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var subscriptionRepository: SubscriptionRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var articlePostProcessorRepository: ArticlePostProcessorRepository

  @Autowired
  lateinit var streamService: StreamService

  @Autowired
  lateinit var releaseThrottleRepository: ReleaseThrottleRepository

  @Scheduled(fixedDelay = 4567)
  fun fillBuckets() {
    val pageable = PageRequest.of(0, 100)
    val buckets = subscriptionRepository.findDueToSubscriptions(Date(), pageable)
      .groupBy { subscription: Subscription -> subscription.bucketId }

    buckets.keys.forEach { bucketId: String? -> processSubscriptionGroup(bucketId!!, buckets[bucketId]!!) }
  }

  fun processSubscriptionGroup(bucketId: String, subscriptions: List<Subscription>) {
    try {
      val bucket = bucketRepository.findById(bucketId).orElseThrow()
      val suggestions = subscriptions
        .map { subscription -> tryAddThrottle(subscription) }
        .map { subscriptionWithThrottle -> suggestArticles(bucket, subscriptionWithThrottle.first, subscriptionWithThrottle.second) }

      if (suggestions.isNotEmpty()) {
        val now = Date()
        suggestions.forEach { subscription -> subscriptionRepository.updateUpdatedAt(subscription.id!!, now) }
      }

    } catch (e: Exception) {
      log.error("Cannot release articles for bucket ${bucketId}")
      e.printStackTrace()
    }
  }

  private fun tryAddThrottle(subscription: Subscription): Pair<Subscription, Optional<ReleaseThrottle>> {
    return Pair(subscription, releaseThrottleRepository.findBySubscriptionId(subscription.id!!))
  }

  private fun suggestArticles(bucket: Bucket, subscription: Subscription, throttleOpt: Optional<ReleaseThrottle>): Subscription {
    val filterExecutorOpt = Optional.ofNullable(bucket.filterExpression).map { filterExpression -> TakeEntryIfRunner(filterExpression.byteInputStream()) }

    val unfiltered = articleRepository.findNewArticlesForSubscription(subscription.id!!)
    val suggestions = if (filterExecutorOpt.isPresent) {
      val filterExecutor = filterExecutorOpt.get()
      val filtered = unfiltered.filter { sourceEntry: Article -> filterExecutor.takeIf(sourceEntry) }
      log.info("Dropped ${unfiltered.size - filtered.size} articles for ${subscription.id}")
      filtered
    } else {
      unfiltered
    }

    val pubDateFn: (article: Article) -> Date;

    fun usePubDate(article: Article): Date { return article.pubDate }
    fun useNow(article: Article): Date { return Date() }

    val releaseable = if (throttleOpt.isPresent) {
      val throttle = throttleOpt.get()
      pubDateFn = ::useNow
      val nextReleaseAt = Date.from(Date().toInstant().plus(Duration.of(1, ChronoUnit.valueOf("Days"))))
      releaseThrottleRepository.updateUpdatedAt(throttle.id!!, Date(), nextReleaseAt)
      suggestions
        .map { article -> scoreArticle(article, throttle.scoreCriteria!!) }
        .sortedByDescending { pair -> pair.second }
        .take(throttle.take)
        .map { pair -> pair.first }

    } else {
      pubDateFn = ::usePubDate
      suggestions
    }

    val postProcessors = articlePostProcessorRepository.findAllByBucketId(bucket.id!!)

    fun setReleaseState(article: Article): Article {
      article.applyPostProcessors = postProcessors.isNotEmpty()
      return article
    }

    releaseable
      .map { article -> setReleaseState(article)}
      .forEach { article -> streamService.addArticleToFeed(article, bucket.streamId!!, bucket.ownerId!!, subscription.tags!!, pubDateFn)}

    return subscription
  }

  private fun scoreArticle(article: Article, scoreCriteria: String): Pair<Article, Double> {
    return Pair(article, 0.0)
  }
}

