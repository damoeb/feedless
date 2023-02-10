package org.migor.rich.rss.config

import org.migor.rich.rss.AppProfiles
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories

@Profile(AppProfiles.elasticsearch)
@Configuration
@EnableElasticsearchRepositories(basePackages = ["org.migor.rich.rss.data.es.repositories"])
@ComponentScan(basePackages = ["org.migor.rich.rss.data.es"] )
class ElasticConfig
