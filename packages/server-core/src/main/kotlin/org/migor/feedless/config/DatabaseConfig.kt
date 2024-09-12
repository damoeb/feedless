package org.migor.feedless.config

import org.migor.feedless.AppLayer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.client.RestTemplate


@Profile(AppLayer.repository)
@Configuration
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories(
  basePackages = ["org.migor.feedless"],
  includeFilters = [ComponentScan.Filter(
    type = FilterType.ASSIGNABLE_TYPE,
    classes = [JpaRepository::class]
  )]
)
@EnableJpaAuditing
class DatabaseConfig {

  @Bean
  fun createRestTemplate(): RestTemplate = RestTemplate()
}
