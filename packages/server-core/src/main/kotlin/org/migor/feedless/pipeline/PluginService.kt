package org.migor.feedless.pipeline

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class PluginService(
  private val entityPlugins: List<MapEntityPlugin<*>>,
  private val transformerPlugins: List<FragmentTransformerPlugin>,
  @Lazy val plugins: List<FeedlessPlugin>
) {

  private val log = LoggerFactory.getLogger(PluginService::class.simpleName)

//  @Autowired
//  private lateinit var defaultMailFormatterService: MailProviderService

  @PostConstruct
  fun postConstruct() {
    log.info("Detected ${transformerPlugins.size} transformer plugins")
    for (plugin in transformerPlugins) {
      log.info("Plugin ${plugin.id()} -> ${plugin::class.simpleName}")
    }
    log.info("Detected ${entityPlugins.size} entity plugins")
    for (plugin in entityPlugins) {
      log.info("Plugin ${plugin.id()} -> ${plugin::class.simpleName}")
    }
    val collidingIds = plugins.groupingBy { it.id() }.eachCount().filter { it.value > 1 }
    if (collidingIds.isNotEmpty()) {
      throw IllegalArgumentException(
        "plugin ids must be unique, the following are not: ${
          collidingIds.keys.joinToString(
            ", "
          )
        } caused by ${plugins.filter { it.id() in collidingIds.keys }.map { it::class.simpleName }}"
      )
    }
  }

  suspend fun findAll(): List<FeedlessPlugin> {
    return withContext(Dispatchers.IO) {
      entityPlugins.plus(transformerPlugins)
    }
  }

  final inline suspend fun <reified T : FeedlessPlugin> resolveById(id: String): T? {
    return plugins.filterTo(ArrayList()) { it: FeedlessPlugin -> it.id() == id }
      .filterIsInstance<T>()
      .firstOrNull()
  }

//  suspend fun resolveMailFormatter(sub: RepositoryEntity): Pair<MailProvider, PluginExecutionParamsInput> {
//    return sub.plugins.mapToPluginInstance<MailProviderPlugin>(this)
//      .firstOrNull() ?: Pair(defaultMailFormatterService, PluginExecutionParamsInput())
//  }
}
