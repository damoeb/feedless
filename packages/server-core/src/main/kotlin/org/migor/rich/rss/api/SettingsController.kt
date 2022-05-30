package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.AppSettingsJsonDto
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingsController {

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @GetMapping("/api/settings")
  fun settings(): AppSettingsJsonDto {
    return AppSettingsJsonDto(
      jsSupport = puppeteerService.canPrerender(),
      stateless = environment.acceptsProfiles(Profiles.of("stateless")),
      webToFeedVersion = propertyService.webToFeedVersion
    )
  }
}
