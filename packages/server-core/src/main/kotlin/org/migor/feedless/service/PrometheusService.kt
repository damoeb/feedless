package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


@Service
@Profile(AppProfiles.prometheus)
class PrometheusService {

  private val log = LoggerFactory.getLogger(PrometheusService::class.simpleName)


}
