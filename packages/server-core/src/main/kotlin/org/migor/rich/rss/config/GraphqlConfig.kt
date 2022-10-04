package org.migor.rich.rss.config

import graphql.scalars.ExtendedScalars
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("database")
class GraphqlConfig {

  @Bean
  fun dateTimeType(): GraphQLScalarType {
    return ExtendedScalars.DateTime
  }

  @Bean
  fun jsonType(): GraphQLScalarType {
    return ExtendedScalars.Json
  }

  @Bean
  fun longType(): GraphQLScalarType {
    return ExtendedScalars.GraphQLLong
  }
}
