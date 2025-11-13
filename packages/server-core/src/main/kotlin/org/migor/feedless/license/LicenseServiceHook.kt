package org.migor.feedless.license

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.license} & ${AppLayer.service}")
class LicenseServiceHook(private val licenseService: JwtLicenseService) : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(JwtLicenseService::class.simpleName)

  override fun onApplicationEvent(event: ApplicationReadyEvent) {
    licenseService.initialize()
  }
}
