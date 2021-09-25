package org.migor.rss.rich.cron

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.database.model.*
import org.migor.rss.rich.database.repository.*
import org.migor.rss.rich.harvest.entryfilter.generated.TakeEntryIfRunner
import org.migor.rss.rich.service.StreamService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.collections.ArrayList


@Component
class FillBucketsCron internal constructor() {

  private val log = LoggerFactory.getLogger(FillBucketsCron::class.simpleName)

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

  @Scheduled(fixedDelay = 2345)
  @Transactional(readOnly = true)
  fun fillBuckets() {
    subscriptionRepository.findDueToSubscriptions(Date())
      .forEach { subscription: Subscription -> processSubscription(subscription) }
  }

  fun processSubscription(subscription: Subscription) {
    try {
      val bucket = bucketRepository.findById(subscription.bucketId!!).orElseThrow()
      suggestArticles(bucket, subscription)

      val now = Date()
      setSubscriptionsUpdatedAt(subscription, now)
      bucketRepository.setLastUpdatedAt(subscription.bucketId!!, now)


    } catch (e: Exception) {
      log.error("Cannot release articles for bucket ${subscription.bucketId}")
      e.printStackTrace()
    }
  }

  private fun setSubscriptionsUpdatedAt(subscription: Subscription, now: Date) {
    log.debug("Updating updatedAt for subscription=${subscription.id}")
    subscriptionRepository.setLastUpdatedAt(subscription.id!!, now)
  }

  private fun suggestArticles(bucket: Bucket, subscription: Subscription): Subscription {
    val filterExecutorOpt = Optional.ofNullable(createTakeIfRunner(bucket.filterExpression))

    val unfiltered = articleRepository.findNewArticlesForSubscription(subscription.id!!).distinctBy { article -> article.url }
    val suggestions = if (filterExecutorOpt.isPresent && unfiltered.isNotEmpty()) {
      val filtered = unfiltered.filter { article: Article -> executeFilter(bucket.filterExpression!!, article) }
      log.info("Dropped ${unfiltered.size - filtered.size} articles for ${subscription.name} (${subscription.id})")
      filtered
    } else {
      unfiltered
    }

    val pubDateFn: (article: Article) -> Date

    fun usePubDate(article: Article): Date {
      return article.pubDate
    }

    fun useNow(article: Article): Date {
      return Date()
    }

//    val releaseable = if (throttleOpt.isPresent) {
//      val throttle = throttleOpt.get()
//      pubDateFn = ::useNow
//      val nextReleaseAt = Date.from(Date().toInstant().plus(Duration.of(1, ChronoUnit.valueOf("Days"))))
//      releaseThrottleRepository.updateUpdatedAt(throttle.id!!, Date(), nextReleaseAt)
//      suggestions
//        .map { article -> scoreArticle(article, throttle.scoreCriteria!!) }
//        .sortedByDescending { pair -> pair.second }
//        .take(throttle.take)
//        .map { pair -> pair.first }
//
//    } else {
      pubDateFn = ::usePubDate
//      suggestions
//    }

    val postProcessors = articlePostProcessorRepository.findAllByBucketId(bucket.id!!)

    fun setReleaseState(article: Article): Article {
      article.applyPostProcessors = postProcessors.isNotEmpty()
      return article
    }

    val tags = ArrayList<NamespacedTag>()
    if (StringUtils.isBlank(subscription.name)) {
      val feed = feedRepository.findByStreamId(subscription.id!!)
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, feed.title!!))
    } else {
      tags.add(NamespacedTag(TagNamespace.SUBSCRIPTION, subscription.name!!))
    }

    subscription.tags?.let { userTags -> tags.addAll(userTags.map { tag -> NamespacedTag(TagNamespace.USER, tag) }) }

    suggestions
      .map { article -> setReleaseState(article) }
      .forEach { article ->
        run {
          this.log.debug("Adding article ${article.url} to bucket ${bucket.title}")
          streamService.addArticleToStream(
            article,
            bucket.streamId!!,
            bucket.ownerId!!,
            tags,
            pubDateFn)
        }
      }

    return subscription
  }

  private fun executeFilter(filterExecutor: String, article: Article): Boolean {
    return createTakeIfRunner(filterExecutor)!!.takeIf(article)
  }

  private fun createTakeIfRunner(filterExpression: String?): TakeEntryIfRunner? {
    return try {
      filterExpression?.let { expr -> TakeEntryIfRunner(expr.byteInputStream()) }
    } catch (e: Exception) {
      log.error("Invalid filter expression ${filterExpression}, ${e.message}")
      null
    }
  }

  private fun scoreArticle(article: Article, scoreCriteria: String): Pair<Article, Double> {
    return Pair(article, 0.0)
  }
}

