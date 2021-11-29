package org.migor.rss.rich.service

import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.generated.MqArticleChange
import org.migor.rss.rich.generated.MqArticleScore
import org.migor.rss.rich.generated.MqAskArticleScore
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ScoreService {

  private val log = LoggerFactory.getLogger(ScoreService::class.simpleName)

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Autowired
  lateinit var articleRepository: ArticleRepository

  fun askForScoring(corrId: String, article: Article) {
    val askScore = MqAskArticleScore.Builder()
      .setUrl(article.url!!)
      .setCorrelationId(corrId)
      .build()
    rabbitTemplate.convertAndSend(RabbitQueue.askArticleScore, JsonUtil.gson.toJson(askScore))
  }

  @RabbitListener(queues = [RabbitQueue.articleScore])
  fun listenArticleScore(articleScoreJson: String) {
    try {
      val articleScore = JsonUtil.gson.fromJson(articleScoreJson, MqArticleScore::class.java)
      val corrId = articleScore.correlationId
      val article = Optional.ofNullable(articleRepository.findByUrl(articleScore.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${articleScore?.url} not found") }
      if (articleScore.error) {
        log.info("[${corrId}] Failed articleScore for ${articleScore.url}")
      } else {
        log.info("[${corrId}] + articleScore for ${articleScore.url}")
        article.score = articleScore.score
        articleRepository.save(article)
      }

      val reportChange = MqArticleChange.builder()
        .setCorrelationId(corrId)
        .setUrl(article.url!!)
        .setReason("score")
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.articleChanged, JsonUtil.gson.toJson(reportChange))
    } catch (e: Exception) {
      this.log.error("Cannot handle articleScore ${e.message}")
    }
  }
}
