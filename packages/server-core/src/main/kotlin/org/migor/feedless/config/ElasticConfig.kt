package org.migor.feedless.config

//import org.springframework.boot.autoconfigure.EnableAutoConfiguration
//import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration
//import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration
//import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration
//import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientAutoConfiguration
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Profile(AppProfiles.elastic)
@Configuration
@EnableElasticsearchRepositories(
  basePackages = ["org.migor.feedless.data.es.repositories"],
)
class ElasticConfig
