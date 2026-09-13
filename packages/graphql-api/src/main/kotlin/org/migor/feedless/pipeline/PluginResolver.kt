package org.migor.feedless.pipeline

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.throttle.Throttled
import org.migor.feedless.generated.types.Plugin
import org.migor.feedless.generated.types.PluginType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile

@DgsComponent
@Profile("${AppProfiles.scrape} & ${AppLayer.api}")
class PluginResolver {

  private val log = LoggerFactory.getLogger(PluginResolver::class.simpleName)

  @Autowired
  private lateinit var pipelinePlugins: PipelinePlugins

  @Throttled
  @DgsQuery
  suspend fun plugins(
    dfe: DataFetchingEnvironment,
  ): List<Plugin> = coroutineScope {
    log.debug("plugins")
    pipelinePlugins.describeAll().map { it.toDto() }
  }
}


internal fun PluginDescriptor.toDto(): Plugin {
  return Plugin(
    id = id,
    name = name,
    listed = listed,
    type =
      if (fragmentTransformer) {
        PluginType.fragment
      } else {
        PluginType.entity
      }

  )
}
