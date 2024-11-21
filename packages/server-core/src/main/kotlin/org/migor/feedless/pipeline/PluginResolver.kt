package org.migor.feedless.pipeline

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.Plugin
import org.migor.feedless.generated.types.PluginType
import org.migor.feedless.session.injectCurrentUser
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@DgsComponent
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.api}")
class PluginResolver {

  private val log = LoggerFactory.getLogger(PluginResolver::class.simpleName)

  @Autowired
  private lateinit var pluginsService: PluginService

  @Throttled
  @DgsQuery
  suspend fun plugins(
    dfe: DataFetchingEnvironment,
  ): List<Plugin> = withContext(injectCurrentUser(currentCoroutineContext(), dfe)) {
    log.debug("plugins")
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
