package org.migor.feedless.pipeline

import jakarta.annotation.PostConstruct
import org.migor.feedless.data.jpa.models.SourceSubscriptionEntity
import org.migor.feedless.generated.types.PluginExecutionParamsInput
import org.migor.feedless.harvest.mapToPluginInstance
import org.migor.feedless.mail.MailProviderService
import org.migor.feedless.pipeline.plugins.MailProvider
import org.migor.feedless.pipeline.plugins.MailProviderPlugin
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class PluginService {

  private val log = LoggerFactory.getLogger(PluginService::class.simpleName)

  @Autowired
  lateinit var entityPlugins: List<MapEntityPlugin>

  @Autowired
  lateinit var transformerPlugins: List<FragmentTransformerPlugin>

  @Autowired
  lateinit var plugins: List<FeedlessPlugin>

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var defaultMailFormatterService: MailProviderService

  @PostConstruct
  fun postConstruct() {
    log.info("Detected ${transformerPlugins.size} transformer plugins")
    for (plugin in transformerPlugins) {
      log.info("Plugin ${plugin.id()}")
    }
    log.info("Detected ${entityPlugins.size} entity plugins")
    for (plugin in entityPlugins) {
      log.info("Plugin ${plugin.id()}")
    }
  }

  fun resolveFragmentTransformerById(pluginId: String): FragmentTransformerPlugin? {
    return transformerPlugins.find { plugin -> plugin.id() == pluginId }
  }

  fun findAll(): List<FeedlessPlugin> {
    return entityPlugins.plus(transformerPlugins)
  }

  final inline fun <reified T : FeedlessPlugin> resolveById(id: String): T? {
    return plugins.filterTo(ArrayList()) { it: FeedlessPlugin -> it.id() == id }
      .filterIsInstance<T>()
      .firstOrNull()
  }

  fun resolveMailFormatter(sub: SourceSubscriptionEntity): Pair<MailProvider, PluginExecutionParamsInput> {
    return sub.plugins.mapToPluginInstance<MailProviderPlugin>(this)
      .firstOrNull() ?: Pair(defaultMailFormatterService, PluginExecutionParamsInput.newBuilder().build())
  }
}
