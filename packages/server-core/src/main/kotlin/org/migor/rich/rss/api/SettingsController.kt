package org.migor.rich.rss.api

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.AppFeatureFlags
import org.migor.rich.rss.api.dto.AppSettings
import org.migor.rich.rss.http.Throttled
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.harvest.PuppeteerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.ResponseEntity
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

  @Value("\${app.publicUrl}")
  lateinit var publicUrl: String

  @Autowired
  lateinit var puppeteerService: PuppeteerService

  @Throttled
  @GetMapping("/api/settings")
  fun settings(): ResponseEntity<AppSettings> {
    val appSettings = AppSettings(
      flags = AppFeatureFlags(
        canPrerender = puppeteerService.canPrerender(),
        hasPuppeteerHost = puppeteerService.hasHost(),
        stateless = environment.acceptsProfiles(Profiles.of("!${AppProfiles.database}")),
        willExtractFulltext = Optional.ofNullable(StringUtils.trimToNull(supportFulltext))
          .map { it.lowercase() == "true" }
          .orElse(false),
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
    "explainFeed" to ApiUrls.explainFeed,
    "discoverFeeds" to ApiUrls.discoverFeeds,
    "webToFeed" to ApiUrls.webToFeed,
  )
}
