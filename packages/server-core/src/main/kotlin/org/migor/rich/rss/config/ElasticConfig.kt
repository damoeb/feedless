package org.migor.rich.rss.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Profile("database")
@Configuration
@EnableElasticsearchRepositories(basePackages = ["org.migor.rich.rss.data.es.repositories"])
@ComponentScan(basePackages = ["org.migor.rich.rss.data.es"] )
class ElasticConfig
