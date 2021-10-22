package org.migor.rss.rich.cron

import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.service.ScoreService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DynArticleScorsCron internal constructor() {

  private val log = LoggerFactory.getLogger(DynArticleScorsCron::class.simpleName)

  @Autowired
  lateinit var scoreService: ScoreService

  @Autowired
  lateinit var articleRepository: ArticleRepository

  //  @Scheduled(fixedDelay = 4567)
//  fun scoreSourceEntries() {
//    val pageable = PageRequest.of(0, 100, Sort.by(Sort.Order.asc("createdAt")))
//    articleRepository.findAllByHasReadabilityAndLastScoredAtIsNull(pageable)
//      .forEach { article -> scoreService.scoreDynamic(article) }
//  }
}
