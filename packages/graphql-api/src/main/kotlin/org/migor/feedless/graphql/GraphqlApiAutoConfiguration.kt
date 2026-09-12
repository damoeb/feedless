package org.migor.feedless.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsDataLoader
import com.netflix.graphql.dgs.DgsDirective
import org.migor.feedless.api.DtoMapperFacade
import org.migor.feedless.config.GraphqlConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType

// Resolvers live in feature packages, so they are found by annotation rather than by package.
@AutoConfiguration
@ComponentScan(
  basePackages = ["org.migor.feedless"],
  useDefaultFilters = false,
  includeFilters = [ComponentScan.Filter(
    type = FilterType.ANNOTATION,
    classes = [DgsComponent::class, DgsDataLoader::class, DgsDirective::class]
  )],
)
@ComponentScan(basePackages = ["org.migor.feedless.api.mapper", "org.migor.feedless.api.graphql"])
// ASSIGNABLE_TYPE instead of @Import keeps the bean names the application's own scan produces.
@ComponentScan(
  basePackages = ["org.migor.feedless.api", "org.migor.feedless.config"],
  useDefaultFilters = false,
  includeFilters = [ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = [DtoMapperFacade::class, GraphqlConfig::class]
  )],
)
class GraphqlApiAutoConfiguration
