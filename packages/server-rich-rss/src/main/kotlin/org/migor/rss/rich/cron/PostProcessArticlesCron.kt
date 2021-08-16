package org.migor.rss.rich.cron

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rss.rich.database.enums.ArticlePostProcessorType
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticlePostProcessor
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.repository.ArticlePostProcessorRepository
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.database.repository.NoFollowUrlRepository
import org.migor.rss.rich.service.ArticleService
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.migor.rss.rich.service.GraphService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*


@Service
class PostProcessArticlesCron internal constructor() {

  private val log = LoggerFactory.getLogger(PostProcessArticlesCron::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var graphService: GraphService

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var noFollowUrlRepository: NoFollowUrlRepository

  @Autowired
  lateinit var postProcessorRepository: ArticlePostProcessorRepository

  val blacklist = listOf("paypal.me", "apple.com", "twitter.com", "patreon.com", "google.com", "amazon.com",
    "paypal.me", "facebook.com", "instagram.com", "tiktok.com")

  //  @Scheduled(fixedDelay = 15000)
  fun postProcessArticlesPerBucket() {
    val pageable = PageRequest.of(0, 100)
    bucketRepository.findDueToPostProcessors(pageable)
      .forEach { bucket: Bucket ->
        handleBucket(bucket)

      }
  }

  private fun handleBucket(bucket: Bucket) {
    try {
      val postProcessors = postProcessorRepository.findAllByBucketId(bucket.id!!)
      this.articleRepository.findAllNewArticlesInBucketId(bucket.id!!, bucket.lastPostProcessedAt)
        .forEach { article -> postProcessors.map { postProcessor -> applyPostProcessor(article, postProcessor, bucket) } }
    } catch (ex: Exception) {
      log.error("Failed to run postProcessors for bucket ${bucket.id}: ${ex.message}")
    } finally {
//      bucketRepository.updateLastPostProcessedAt(bucket.id!!, Date());
    }
  }

  private fun applyPostProcessor(article: Article, postProcessor: ArticlePostProcessor, bucket: Bucket) {
    try {
      when (postProcessor.type) {
        ArticlePostProcessorType.FOLLOW_LINKS -> followLinks(article, bucket)
      }
    } catch (e: Exception) {
      log.error("Failed to run postProcessor ${postProcessor.type} for article ${article.url}: ${e.message}")
    }
  }

  private fun followLinks(article: Article, bucket: Bucket) {
    if (article.hasReadability == true) {
      try {
        if (article.readability == null) {
          log.error("Readability is null but hasReadability=true for ${article.url}")
        } else {
          val doc = Jsoup.parse(article.readability!!.content)
          val urls = doc.body().select("a[href]")
            .map { link -> absUrl(article.url!!, link.attr("href")) }
            .distinct()
            .filter { url -> StringUtils.isNotBlank(url) }
            .filter { url -> isQualifiedUrl(url) }

          urls.forEach { url -> graphService.link(article.url!!, url) }

          val groups = urls.groupBy { url -> URL(url).host }
          val firstUrlPerDomain = groups.keys.map { domain -> groups[domain]!!.first() }

          val toSeedFromUrls = Stack<String>()
          toSeedFromUrls.addAll(firstUrlPerDomain)
          toSeedFromUrls.shuffled()

          var seeded = 0
          while (seeded < 1 && toSeedFromUrls.isNotEmpty()) {
            val url = toSeedFromUrls.pop()
            val success = articleService.tryCreateArticleFromContainedUrlForBucket(url, article.url!!, bucket)
            if (success) {
              log.info("Seeded article from $url to bucket ${bucket.id}")
              seeded++
            }
          }

        }
      } catch (e: Exception) {
        log.error("Failed to followLinks for ${article.url}: ${e.message}")
      }
    }
  }

  private fun isQualifiedUrl(url: String): Boolean {
    try {
      val parsed = URL(url)
      if (StringUtils.isBlank(parsed.path.replace("/", ""))) {
        return false
      }

      return blacklist.none { blacklistedUrl -> url.contains(blacklistedUrl) } && !noFollowUrlRepository.existsByUrlStartingWith(url)
    } catch (e: Exception) {
      return false
    }
  }

}

