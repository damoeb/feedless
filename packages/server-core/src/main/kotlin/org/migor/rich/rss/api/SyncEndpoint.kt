package org.migor.rich.rss.api

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class SyncEndpoint {

  private val log = LoggerFactory.getLogger(SyncEndpoint::class.simpleName)

  @Value("\${CORE_VERSION}")
  lateinit var version: String

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @GetMapping("/api/sync")
  fun sync(
    @RequestHeader("x-node-id", required = true) nodeId: String,
    @RequestHeader("x-version", required = true) version: String,
    @RequestHeader("x-url", required = true) url: String,
  ): ResponseEntity<String> {
    log.info("sync version=${version} nodeId=${nodeId} url=${url}")
    meterRegistry.counter("servers/sync").increment()
    return if (version == this.version) {
      ResponseEntity.ok("UP-TO-DATE")
    } else {
      ResponseEntity.ok("Version ${this.version} is available")
    }
  }
}
