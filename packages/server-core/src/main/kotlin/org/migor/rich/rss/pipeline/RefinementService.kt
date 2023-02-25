package org.migor.rich.rss.pipeline

import jakarta.annotation.PostConstruct
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.ContentWithContext
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.RefinementEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
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
      context: ContentWithContext,
  ): ContentEntity {
    return context.content
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
