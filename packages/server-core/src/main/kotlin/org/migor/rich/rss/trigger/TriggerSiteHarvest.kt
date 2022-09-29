package org.migor.rich.rss.trigger

import org.migor.rich.rss.database2.models.ArticleEntity
import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.migor.rich.rss.database2.repositories.ArticleDAO
import org.migor.rich.rss.service.FulltextService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("database2")
class TriggerSiteHarvest internal constructor() {

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var fulltextService: FulltextService

  @Scheduled(fixedDelay = 3245)
  @Transactional(readOnly = true)
  fun fillExporters() {
    articleDAO.findHarvestableArticles()
      .map { Pair(it[0] as ArticleEntity, it[1] as NativeFeedEntity) }
      .forEach { (article, feed) -> fulltextService.extractFulltext(newCorrId(), article, feed) }
  }
}
