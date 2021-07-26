package org.migor.rss.rich.cron

import org.jsoup.Jsoup
import org.migor.rss.rich.database.enums.ArticlePostProcessorType
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticlePostProcessor
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.repository.ArticlePostProcessorRepository
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.service.ArticleService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service


@Service
class PostProcessArticlesCron internal constructor() {

  private val log = LoggerFactory.getLogger(PostProcessArticlesCron::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var postProcessorRepository: ArticlePostProcessorRepository

  @Scheduled(fixedDelay = 4567)
  fun postProcessArticlesPerBucket() {
    val pageable = PageRequest.of(0, 100)
    bucketRepository.findDueToPostProcessors(pageable)
      .forEach { bucket: Bucket ->
        try {
          handleBucket(bucket)
        } catch (ex: Exception) {
          log.error("Failed to postProcessArticlesPerBucket: ${ex.message}")
        }
      }
  }

  private fun handleBucket(bucket: Bucket) {
    val postProcessors = postProcessorRepository.findAllByBucketId(bucket.id!!)
    this.articleRepository.findAllNewArticlesInBucketId(bucket.id!!, bucket.lastPostProcessedAt)
      .forEach { article -> postProcessors.forEach { postProcessor -> applyPostProcessor(article, postProcessor) } }
  }

  private fun applyPostProcessor(article: Article, postProcessor: ArticlePostProcessor) {
    when(postProcessor.type) {
      ArticlePostProcessorType.FOLLOW_LINKS -> followLinks(article)
//      ArticlePostProcessorType.FULLTEXT -> extractFulltext(article)
    }
  }

  private fun followLinks(article: Article) {
    if (article.hasReadability == true) {
      val doc = Jsoup.parse(article.readability!!.content)
      val urls = doc.body().select("a[href]").map { link -> link.absUrl("href") }.distinct()
      log.info("Found ${urls.size} links")
    }
  }

}

