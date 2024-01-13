package org.migor.feedless.service

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppProfiles
import org.migor.feedless.plugins.EntityTransformerPlugin
import org.migor.feedless.plugins.FeedlessPlugin
import org.migor.feedless.plugins.FragmentTransformerPlugin
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class PluginService {

  private val log = LoggerFactory.getLogger(PluginService::class.simpleName)

  @Autowired
  lateinit var entityPlugins: List<EntityTransformerPlugin>

  @Autowired
  lateinit var transformerPlugins: List<FragmentTransformerPlugin>

  @Autowired
  lateinit var environment: Environment

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

  fun resolveEntityTransformerById(pluginId: String): EntityTransformerPlugin? {
    return entityPlugins.find { plugin -> plugin.id() == pluginId }
  }

  fun findAll(): List<FeedlessPlugin> {
    return entityPlugins.plus(transformerPlugins)
  }

}
