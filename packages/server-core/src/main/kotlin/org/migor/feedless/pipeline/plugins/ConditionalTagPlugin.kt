package org.migor.feedless.pipeline.plugins

import com.google.gson.Gson
import kotlinx.coroutines.currentCoroutineContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.migor.feedless.user.corrId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class ConditionalTagPlugin : MapEntityPlugin<ConditionalTagPluginParams> {

  private val log = LoggerFactory.getLogger(ConditionalTagPlugin::class.simpleName)

  @Autowired
  private lateinit var filterPlugin: CompositeFilterPlugin


  override fun id(): String = FeedlessPlugins.org_feedless_conditional_tag.name
  override fun name(): String = "Conditional Tags"

  override fun listed() = true
  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    params: ConditionalTagPluginParams,
    logCollector: LogCollector
  ): Document {
    log.debug("mapEntity ${document.url}")
    val newTags = params.filter {
      filterPlugin.matches(document.asJsonItem(), it.filter, 0)
    }.map { it.tag }.toMutableSet()

    return if (newTags.isNotEmpty()) {
      document.tags?.let { newTags.addAll(it) }
      document.copy(
        tags = newTags.distinct().sorted().toTypedArray()
      )
    } else {
      document
    }
  }

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    paramsJson: String?,
    logCollector: LogCollector
  ): Document {
    return mapEntity(document, repository, fromJson(paramsJson), logCollector)
  }

  override suspend fun fromJson(jsonParams: String?): ConditionalTagPluginParams {
    return Gson().fromJson(jsonParams, ConditionalTagPluginParams::class.java)
  }
}
