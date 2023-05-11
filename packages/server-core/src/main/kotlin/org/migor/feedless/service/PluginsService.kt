package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.trigger.plugins.WebDocumentPlugin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class PluginsService {

  @Autowired
  lateinit var allPlugins: List<WebDocumentPlugin>

  @Autowired
  lateinit var environment: Environment

  fun resolvePlugins(plugins: List<String>): List<WebDocumentPlugin> {
    val resolved = mutableListOf<WebDocumentPlugin>()
    resolved.addAll(availablePlugins().filter { plugins.contains(it.id()) })
    resolved.addAll(defaultPlugins())
    return resolved.distinctBy { it.id() }
  }

  fun availablePlugins(): List<WebDocumentPlugin> {
    return allPlugins.filter { it.enabled() }
  }

  fun defaultPlugins(): List<WebDocumentPlugin> {
    return availablePlugins().filter { !it.configurableByUser() }
  }

}
