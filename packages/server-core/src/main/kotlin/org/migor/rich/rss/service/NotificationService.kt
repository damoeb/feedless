package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.ArticleRefType
import org.migor.rss.rich.database.model.Feed
import org.migor.rss.rich.database.repository.ArticleRefRepository
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.database.repository.BucketRepository
import org.migor.rss.rich.database.repository.UserRepository
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class NotificationService {

  private val log = LoggerFactory.getLogger(NotificationService::class.simpleName)

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var articleRepository: ArticleRepository

  @Autowired
  lateinit var articleRefRepository: ArticleRefRepository

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  fun createOpsNotificationForUser(corrId: String, feed: Feed, e: Throwable) {

    this.log.info("[${corrId}] Creating ops-notification userId=${feed.ownerId} feedName=${feed.title} feedId=${feed.id}")

    val article = Article()
    val message = Optional.ofNullable(e.message).orElse(e.javaClass.toString())
    val json = JsonUtil.gson.toJson(message)
    article.contentText = json
    article.url = "${propertyService.host}/feed/${feed.id}?errorFrom=${Date()}" // todo mag take to feed management

    article.title = "Problems with feed ${Optional.ofNullable(feed.title).orElse(feed.feedUrl)}"
    val savedArticle = articleRepository.save(article)

    exporterTargetService.pushArticleToTargets(
      corrId,
      savedArticle,
      feed.streamId!!,
      ArticleRefType.ops,
      feed.ownerId,
      Date(),
      null,
      targets = emptyList()
    )
  }

}
