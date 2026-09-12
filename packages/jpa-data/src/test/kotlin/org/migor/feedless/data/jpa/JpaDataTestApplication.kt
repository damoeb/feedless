package org.migor.feedless.data.jpa

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// Mirrors server-core's FeedlessApplication + DatabaseConfig for the persistence layer only.
@SpringBootApplication(
  scanBasePackages = ["org.migor.feedless.data.jpa"],
  exclude = [JpaRepositoriesAutoConfiguration::class]
)
@EntityScan(basePackages = ["org.migor.feedless.data.jpa"])
@EnableJpaRepositories(
  basePackages = ["org.migor.feedless.data.jpa"],
  includeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = [JpaRepository::class])],
)
@EnableJpaAuditing
class JpaDataTestApplication
