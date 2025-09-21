package org.migor.feedless.config

import com.netflix.graphql.dgs.DgsDirective
import com.netflix.graphql.dgs.context.DgsCustomContextBuilder
import graphql.schema.DataFetcherFactories
import graphql.schema.DataFetchingEnvironment
import graphql.schema.GraphQLFieldDefinition
import graphql.schema.idl.SchemaDirectiveWiring
import graphql.schema.idl.SchemaDirectiveWiringEnvironment
import org.apache.commons.validator.EmailValidator
import org.migor.feedless.AppLayer
import org.migor.feedless.common.CacheKeyGenerator
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.StringLiteralOrVariableInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import us.codecraft.xsoup.Xsoup
import java.net.URI

@Configuration
@Profile(AppLayer.api)
class GraphqlConfig {

  @Bean
  @Qualifier("cacheKeyGenerator")
  fun cacheKeyGenerator(): CacheKeyGenerator {
    return CacheKeyGenerator()
  }

  @Component
  class CustomContextBuilder : DgsCustomContextBuilder<DgsCustomContext> {
    override fun build(): DgsCustomContext {
      return DgsCustomContext()
    }
  }
}

class DgsCustomContext {
  var repositoryId: RepositoryId? = null
  var documentId: DocumentId? = null
  var userId: UserId? = null
}

@Profile(AppLayer.api)
@DgsDirective(name = "url")
class UrlValidatorDirective : SchemaDirectiveWiring {
  override fun onField(env: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
    val fieldsContainer = env.fieldsContainer
    val fieldDefinition = env.fieldDefinition

    val originalDataFetcher = env.codeRegistry.getDataFetcher(fieldsContainer, fieldDefinition)
    val dataFetcher = DataFetcherFactories.wrapDataFetcher(
      originalDataFetcher
    ) { _: DataFetchingEnvironment?, value: Any? ->
      if (value is String) {
        URI(value)
        return@wrapDataFetcher value
      }
      if (value is StringLiteralOrVariableInput) {
        value.literal?.let {
          try {
            URI(it)
          } catch (e: Exception) {
            throw IllegalArgumentException("not a url")
          }
        }
        return@wrapDataFetcher value
      }


      value
    }

    env.codeRegistry.dataFetcher(fieldsContainer, fieldDefinition, dataFetcher)

    return fieldDefinition
  }
}

@Profile(AppLayer.api)
@DgsDirective(name = "xpath")
class XPathValidatorDirective : SchemaDirectiveWiring {
  override fun onField(env: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
    val fieldsContainer = env.fieldsContainer
    val fieldDefinition = env.fieldDefinition

    val originalDataFetcher = env.codeRegistry.getDataFetcher(fieldsContainer, fieldDefinition)
    val dataFetcher = DataFetcherFactories.wrapDataFetcher(
      originalDataFetcher
    ) { _: DataFetchingEnvironment?, value: Any? ->
      if (value is String) {
        try {
          Xsoup.compile(value)
        } catch (e: Exception) {
          throw IllegalArgumentException("not an xpath")
        }

        return@wrapDataFetcher value
      }

      value
    }

    env.codeRegistry.dataFetcher(fieldsContainer, fieldDefinition, dataFetcher)

    return fieldDefinition
  }
}

@Profile(AppLayer.api)
@DgsDirective(name = "email")
class EmailValidatorDirective : SchemaDirectiveWiring {
  override fun onField(env: SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition>): GraphQLFieldDefinition {
    val fieldsContainer = env.fieldsContainer
    val fieldDefinition = env.fieldDefinition

    val originalDataFetcher = env.codeRegistry.getDataFetcher(fieldsContainer, fieldDefinition)
    val dataFetcher = DataFetcherFactories.wrapDataFetcher(
      originalDataFetcher
    ) { _: DataFetchingEnvironment?, value: Any? ->
      if (!EmailValidator.getInstance().isValid(value as String)) {
        throw IllegalArgumentException("invalid email")
      }

      value
    }

    env.codeRegistry.dataFetcher(fieldsContainer, fieldDefinition, dataFetcher)

    return fieldDefinition
  }
}
