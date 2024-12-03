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
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.prod} & ${AppLayer.service}")
class PostStartupVerificationService {

  private val log = LoggerFactory.getLogger(PostStartupVerificationService::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @PostConstruct
  fun postConstruct() {
    verifyGeneralEnvironment()
    verifySaasEnvironment()
  }

  private fun verifyGeneralEnvironment() {
    verifyEnvironment("general", arrayOf(AppProfiles.scrape, AppLayer.scheduler, AppLayer.repository, AppLayer.api, AppProfiles.standaloneFeeds))
  }

  private fun verifySaasEnvironment() {
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.saas))) {
      verifyEnvironment("saas", arrayOf(AppProfiles.telegram, AppProfiles.seed, AppProfiles.standaloneFeeds, AppProfiles.analytics ))
    }
  }

  private fun verifyEnvironment(envName: String, expectedProfiles: Array<String>) {
    log.info("Validating $envName environment")
    val missingProfiles = expectedProfiles.filter { !environment.acceptsProfiles(Profiles.of(it)) }
    if (missingProfiles.isNotEmpty()) {
      throw IllegalArgumentException("The following spring-profiles are not activated ${missingProfiles.joinToString(",")}")
    }
  }

}
