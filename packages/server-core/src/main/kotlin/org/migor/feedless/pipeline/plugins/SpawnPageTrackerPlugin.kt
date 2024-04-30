package org.migor.feedless.pipeline.plugins

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.pipeline.MapEntityPlugin
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppProfiles.database}")
class SpawnPageTrackerPlugin : MapEntityPlugin {

  private val log = LoggerFactory.getLogger(SpawnPageTrackerPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_spawn_page_tracker.name
  override fun name(): String = "Spawn Page Tacker"

  override fun listed() = true

  override fun mapEntity(
    corrId: String,
    webDocument: WebDocumentEntity,
    subscription: SourceSubscriptionEntity,
    params: PluginExecutionParamsInput
  ) {
    log.info("[$corrId] mapEntity ${webDocument.url}")
    // todo create new sourcesubscription tracking this page for n-days
  }
}
