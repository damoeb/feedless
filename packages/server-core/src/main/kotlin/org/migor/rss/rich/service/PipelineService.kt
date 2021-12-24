package org.migor.rss.rich.service

import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.repository.ArticleRepository
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.migor.rss.rich.pipeline.ArticleHook
import org.migor.rss.rich.pipeline.FilterHook
import org.migor.rss.rich.pipeline.ShellCommandHook
import org.migor.rss.rich.pipeline.TaggerHook
import org.migor.rss.rich.pipeline.YtArchiverHook
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class PipelineService internal constructor() {

  private lateinit var hookImplementations: List<ArticleHook>
  private val log = LoggerFactory.getLogger(PipelineService::class.simpleName)

  @Autowired
  lateinit var articleRepository: ArticleRepository

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

  fun triggerPipeline(
    corrId: String,
    hookSpecs: List<ArticleHookSpec>,
    article: ArticleSnapshot,
    bucket: Bucket
  ): Triple<ArticleSnapshot, List<NamespacedTag>, Map<String, String>>? {
    val additionalData = mutableMapOf<String, String>()
    val tags = mutableListOf<NamespacedTag>()
    val applied = hookSpecs.takeWhile { hookSpec ->
      hookImplementations.first { hookImplementation -> hookImplementation.type() == hookSpec.type }
        .process(
          corrId,
          article,
          bucket,
          hookSpec,
          { tag: NamespacedTag -> tags.add(tag) }
        ) { data: Pair<String, String> -> additionalData.putIfAbsent(data.first, data.second) }
    }
    return if (applied.size == hookSpecs.size) {
      Triple(article, tags, additionalData)
    } else {
      null
    }
  }
}
