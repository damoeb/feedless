package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.ArticleWithContext
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.RefinementEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
@Profile("database")
class RefinementService internal constructor() {

  private lateinit var hookImplementations: List<PreImportAction>
  private val log = LoggerFactory.getLogger(RefinementService::class.simpleName)

  @Autowired
  lateinit var shellCommandHook: ShellCommandHook

  @Autowired
  lateinit var ytArchiverHook: YtArchiverHook

  @PostConstruct
  fun onInit() {
    hookImplementations = listOf(shellCommandHook, ytArchiverHook)
  }

  fun triggerRefinement(
    corrId: String,
    refinements: List<RefinementEntity>,
    context: ArticleWithContext,
  ): ArticleEntity {
    return context.article
//    val applied = refinements.takeWhile { refinement ->
//      hookImplementations.first { hookImplementation -> hookImplementation.type() == refinement.type }
//        .process(
//          corrId,
//          article,
//          bucket,
//          refinement,
//    }
//    return if (applied.size == refinements.size) {
//      Triple(article, tags, additionalData)
//    } else {
//      null
//    }
  }
}
