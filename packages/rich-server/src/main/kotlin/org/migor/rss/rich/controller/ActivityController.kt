package org.migor.rss.rich.controller

import org.migor.rss.rich.service.ActivityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate


@RestController
class ActivityController {

  @Autowired
  lateinit var activityService: ActivityService

  @GetMapping("/group:{groupId}/activity")
  fun activityForGroup(@PathVariable("groupId") groupId: String): Map<LocalDate, Int> {
    return activityService.findLatestActivityBySubscriptionGroupId(groupId)
  }

  @GetMapping("/subscription:{subscriptionId}/activity")
  fun activityForSubscription(@PathVariable("subscriptionId") subscriptionId: String): Map<LocalDate, Int> {
    return activityService.findLatestActivityBySubscriptionId(subscriptionId)
  }

  @RequestMapping("/source:{sourceId}/activity")
  fun activityForSource(@PathVariable("sourceId") sourceId: String): Map<LocalDate, Int> {
    return activityService.findLatestActivityBySourceId(sourceId)
  }

  @GetMapping("/group:{groupId}/activity.svg", produces = [MediaType.APPLICATION_XML_VALUE])
  fun activitySvgForGroup(@PathVariable("groupId") groupId: String): String {
    return toSvg(activityService.findLatestActivityBySubscriptionGroupId(groupId))
  }

  @GetMapping("/subscription:{subscriptionId}/activity.svg", produces = [MediaType.APPLICATION_XML_VALUE])
  fun activitySvgForSubscription(@PathVariable("subscriptionId") subscriptionId: String): String {
    return toSvg(activityService.findLatestActivityBySubscriptionId(subscriptionId))
  }

  @RequestMapping("/source:{sourceId}/activity.svg", produces = [MediaType.APPLICATION_XML_VALUE])
  fun activitySvgForSource(@PathVariable("sourceId") sourceId: String): String {
    return toSvg(activityService.findLatestActivityBySourceId(sourceId))
  }

  private fun toSvg(activity: Map<LocalDate, Int>): String {
    return """<?xml version="1.0" encoding="UTF-8"?>
    <svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="100" height="100%">")
    <circle cx="50" cy="50" r="30" fill="red"></circle>
    </svg>
    """
  }
}
