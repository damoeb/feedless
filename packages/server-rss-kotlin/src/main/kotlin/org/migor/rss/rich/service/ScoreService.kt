package org.migor.rss.rich.service

import org.migor.rss.rich.config.RabbitQueue
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.generated.MqArticleScore
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ScoreService {

  private val log = LoggerFactory.getLogger(ScoreService::class.simpleName)

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Autowired
  lateinit var articleRepository: ArticleRepository

  fun askForScoring(article: Article) {
    rabbitTemplate.convertAndSend(RabbitQueue.askArticleScore, article.url!!)
  }

  @RabbitListener(queues = [RabbitQueue.articleScore])
  fun listenArticleScore(articleScoreJson: String) {
    try {
      val articleScore = JsonUtil.gson.fromJson(articleScoreJson, MqArticleScore::class.java)

      val article = articleRepository.findByUrl(articleScore.url!!).orElseThrow { throw IllegalArgumentException("Article ${articleScore?.url} not found") }
      if (articleScore.error) {
        log.info("Failed articleScore for ${articleScore.url}")
      } else {
        log.info("+ articleScore for ${articleScore.url}")
        article.score = articleScore.score
        articleRepository.save(article)
      }
      // todo mag fix subscription updated at, so bucket filling will be after articles are scored
      rabbitTemplate.convertAndSend(RabbitQueue.articleChanged, arrayOf(article.url!!, "score"))
    } catch (e: Exception) {
      this.log.error("Cannot handle articleScore ${e.message}")
    }
  }
}
