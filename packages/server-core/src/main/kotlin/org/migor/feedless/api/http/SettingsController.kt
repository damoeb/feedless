package org.migor.feedless.api.http

import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.Throttled
import org.migor.feedless.api.dto.AppFeatureFlags
import org.migor.feedless.api.dto.AppSettings
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.PuppeteerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

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

  @Throttled
  @GetMapping("/api/settings")
  fun settings(): ResponseEntity<AppSettings> {
    val appSettings = AppSettings(
      flags = AppFeatureFlags(
        canPrerender = puppeteerService.canPrerender(),
        stateless = environment.acceptsProfiles(Profiles.of("!${AppProfiles.database}")),
        willExtractFulltext = StringUtils.trimToNull(supportFulltext)?.lowercase() == "true",
        canMail = false,
        canPush = false,
      ),
      urls = geApiUrls(),
      webToFeedVersion = propertyService.webToFeedVersion,
    )
    return ResponseEntity.ok(appSettings)
  }

  private fun geApiUrls(): Map<String, String> = mapOf(
    "standaloneFeed" to ApiUrls.standaloneFeed,
    "transformFeed" to ApiUrls.transformFeed,
//    "explainFeed" to ApiUrls.explainFeed,
    "discoverFeeds" to ApiUrls.discoverFeeds,
    "webToFeed" to ApiUrls.webToFeedFromRule,
  )
}
