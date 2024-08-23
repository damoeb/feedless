package org.migor.feedless.pipeline

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.Plugin
import org.migor.feedless.generated.types.PluginType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile("${AppProfiles.api} & ${AppProfiles.scrape}")
class PluginResolver {

  private val log = LoggerFactory.getLogger(PluginResolver::class.simpleName)

  @Autowired
  private lateinit var pluginsService: PluginService

  @Throttled
  @DgsQuery
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  suspend fun plugins(
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Plugin> = coroutineScope {
    log.debug("[$corrId] plugins")
    pluginsService.findAll().map { it.toDto() }
  }
}


private fun FeedlessPlugin.toDto(): Plugin {
  return Plugin(
    id = id(),
    name = name(),
    listed = listed(),
    type =
    if (this is FragmentTransformerPlugin) {
      PluginType.fragment
    } else {
      PluginType.entity
    }

  )
}
