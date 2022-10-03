package org.migor.rich.rss.service

import org.migor.rich.rss.config.RabbitQueue
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.generated.MqAskArticleScore
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("database")
class ScoreService {

  private val log = LoggerFactory.getLogger(ScoreService::class.simpleName)

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  fun askForScoring(corrId: String, article: ArticleEntity, feed: NativeFeedEntity) {
    val askScore = MqAskArticleScore.Builder()
      .setArticleUrl(article.url!!)
      .setFeedId(feed.id.toString())
      .setCorrelationId(corrId)
      .build()
    rabbitTemplate.convertAndSend(RabbitQueue.askArticleScore, JsonUtil.gson.toJson(askScore))
  }

//  @RabbitListener(queues = [RabbitQueue.askArticleScore])
//  fun scoreStatic(articleScoreJson: String) {
//    val request = JsonUtil.gson.fromJson(articleScoreJson, MqAskArticleScore::class.java)
//    val articleUrl = request.articleUrl
//    val article = Optional.ofNullable(articleRepository.findByUrl(articleUrl!!))
//      .orElseThrow { throw IllegalArgumentException("Article ${articleUrl} not found") }
//
//  }
}
