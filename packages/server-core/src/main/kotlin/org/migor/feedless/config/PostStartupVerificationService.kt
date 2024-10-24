package org.migor.feedless.config

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.prod} & ${AppLayer.service}")
class PostStartupVerificationService {

  private val log = LoggerFactory.getLogger(PostStartupVerificationService::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @PostConstruct
  fun postConstruct() {
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
      log.info("Validating environment")
      val expectedSaasProfiles = arrayOf(AppProfiles.telegram, AppProfiles.seed, AppProfiles.legacyFeeds)
      val missingProfiles = expectedSaasProfiles.filter { !environment.acceptsProfiles(Profiles.of(it)) }
      if (missingProfiles.isNotEmpty()) {
        throw IllegalArgumentException("The following spring-profiles are not activated ${missingProfiles.joinToString(",")}")
      }
    }
  }

}
