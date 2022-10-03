package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.NamespacedTag
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.RefinementEntity
import org.migor.rich.rss.harvest.ArticleSnapshot
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
@Profile("database")
class RefinementService internal constructor() {

  private lateinit var hookImplementations: List<PipelineHook>
  private val log = LoggerFactory.getLogger(RefinementService::class.simpleName)

  @Autowired
  lateinit var shellCommandHook: ShellCommandHook

  @Autowired
  lateinit var ytArchiverHook: YtArchiverHook

  @Autowired
  lateinit var filterHook: FilterHook

  @Autowired
  lateinit var taggerHook: TaggerHook

  @PostConstruct
  fun onInit() {
    hookImplementations = listOf(shellCommandHook, ytArchiverHook, filterHook, taggerHook)
  }

  fun triggerRefinement(
    corrId: String,
    refinements: List<RefinementEntity>,
    article: ArticleSnapshot,
    bucket: BucketEntity
  ): Triple<ArticleSnapshot, List<NamespacedTag>, Map<String, String>>? {
    val additionalData = mutableMapOf<String, String>()
    val tags = mutableListOf<NamespacedTag>()
    val applied = refinements.takeWhile { refinement ->
      hookImplementations.first { hookImplementation -> hookImplementation.type() == refinement.type }
        .process(
          corrId,
          article,
          bucket,
          refinement,
          { tag: NamespacedTag -> tags.add(tag) }
        ) { data: Pair<String, String> -> additionalData.putIfAbsent(data.first, data.second) }
    }
    return if (applied.size == refinements.size) {
      Triple(article, tags, additionalData)
    } else {
      null
    }
  }
}
