package org.migor.rich.rss.api

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.AppSettingsJsonDto
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.PuppeteerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class SettingsController {

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var environment: Environment

  @Value("\${app.enableFulltextExtraction}")
  lateinit var supportFulltext: String

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @GetMapping("/api/settings")
  fun settings(): AppSettingsJsonDto {
    return AppSettingsJsonDto(
      canPrerender = puppeteerService.canPrerender(),
      hasPuppeteerHost = puppeteerService.hasHost(),
      stateless = environment.acceptsProfiles(Profiles.of("stateless")),
      willExtractFulltext = Optional.ofNullable(StringUtils.trimToNull(supportFulltext))
        .map { it.lowercase() == "true" }
        .orElse(false),
      canMail = false,
      canPush = false,
      webToFeedVersion = propertyService.webToFeedVersion
    )
  }
}
