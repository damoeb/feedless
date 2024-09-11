package org.migor.feedless.pipeline

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.Plugin
import org.migor.feedless.generated.types.PluginType
import org.migor.feedless.session.useRequestContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile("${AppProfiles.scrape} & ${AppLayer.api}")
class PluginResolver {

  private val log = LoggerFactory.getLogger(PluginResolver::class.simpleName)

  @Autowired
  private lateinit var pluginsService: PluginService

  @Throttled
  @DgsQuery
  @Transactional
  suspend fun plugins(
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): List<Plugin> = withContext(useRequestContext(currentCoroutineContext())) {
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
