package org.migor.rich.rss.cron

import org.migor.rich.rss.database.repository.ArticleRepository
import org.migor.rich.rss.service.ScoreService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("stateful")
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
