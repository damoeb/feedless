package org.migor.rich.rss.service

import org.migor.rich.rss.database.models.NativeFeedEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("database2")
class NotificationService {

  private val log = LoggerFactory.getLogger(NotificationService::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var exporterTargetService: ExporterTargetService

  fun createOpsNotificationForUser(corrId: String, feed: NativeFeedEntity, e: Throwable) {

//    todo mag
//    this.log.info("[${corrId}] Creating ops-notification userId=${feed.ownerId} feedName=${feed.title} feedId=${feed.id}")
//
//    val article = Article()
//    val message = Optional.ofNullable(e.message).orElse(e.javaClass.toString())
//    val json = JsonUtil.gson.toJson(message)
//    article.contentText = json
//    article.url = "${propertyService.publicUrl}/feed/${feed.id}?errorFrom=${Date()}" // todo mag take to feed management
//
//    article.title = "Problems with feed ${Optional.ofNullable(feed.title).orElse(feed.feedUrl)}"
//    val savedArticle = articleService.save(article)
//
//    exporterTargetService.pushArticleToTargets(
//      corrId,
//      savedArticle,
//      feed.streamId!!,
//      ArticleRefType.ops,
//      feed.ownerId,
//      Date(),
//      null,
//      targets = emptyList()
//    )
  }

}
