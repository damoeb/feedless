package org.migor.feedless.pipeline.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.RepositoryEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.scrape)
class ConditionalTagPlugin : MapEntityPlugin {

  private val log = LoggerFactory.getLogger(ConditionalTagPlugin::class.simpleName)

  @Autowired
  lateinit var filterPlugin: CompositeFilterPlugin


  override fun id(): String = FeedlessPlugins.org_feedless_conditional_tag.name
  override fun name(): String = "Conditional Tags"

  override fun listed() = true

  override fun mapEntity(
    corrId: String,
    document: DocumentEntity,
    repository: RepositoryEntity,
    params: PluginExecutionParamsInput
  ): DocumentEntity {
    log.info("[$corrId] mapEntity ${document.url}")
    val newTags = params.org_feedless_conditional_tag.filter {
      filterPlugin.matches(document, it.filter, 0)
    }.map { it.tag }.toMutableSet()

    if (newTags.isNotEmpty()) {
      document.tags?.let { newTags.addAll(it) }
      document.tags = newTags.distinct().sorted().toTypedArray()
    }

    return document
  }
}
