package org.migor.feedless.pipeline

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class PluginService {

  private val log = LoggerFactory.getLogger(PluginService::class.simpleName)

  @Autowired
  private lateinit var entityPlugins: List<MapEntityPlugin>

  @Autowired
  private lateinit var transformerPlugins: List<FragmentTransformerPlugin>

  @Lazy
  @Autowired
  lateinit var plugins: List<FeedlessPlugin>

//  @Autowired
//  private lateinit var defaultMailFormatterService: MailProviderService

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

  suspend fun findAll(): List<FeedlessPlugin> {
    return withContext(Dispatchers.IO) {
      entityPlugins.plus(transformerPlugins)
    }
  }

  final inline fun <reified T : FeedlessPlugin> resolveById(id: String): T? {
    return plugins.filterTo(ArrayList()) { it: FeedlessPlugin -> it.id() == id }
      .filterIsInstance<T>()
      .firstOrNull()
  }

//  suspend fun resolveMailFormatter(sub: RepositoryEntity): Pair<MailProvider, PluginExecutionParamsInput> {
//    return sub.plugins.mapToPluginInstance<MailProviderPlugin>(this)
//      .firstOrNull() ?: Pair(defaultMailFormatterService, PluginExecutionParamsInput())
//  }
}
