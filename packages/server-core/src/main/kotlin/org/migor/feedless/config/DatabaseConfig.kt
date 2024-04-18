package org.migor.feedless.config

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.transaction.annotation.EnableTransactionManagement


@Profile(AppProfiles.database)
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
class DatabaseConfig
