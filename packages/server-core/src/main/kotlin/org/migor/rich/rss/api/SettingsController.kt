package org.migor.rich.rss.api

import org.migor.rich.rss.api.dto.AppSettingsJsonDto
import org.migor.rich.rss.service.PropertyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SettingsController {

  @Autowired
  lateinit var propertyService: PropertyService

  @GetMapping("/api/settings")
  fun settings(): AppSettingsJsonDto {
    return AppSettingsJsonDto(jsSupport = false, webToFeedVersion = propertyService.webToFeedVersion)
  }
}
