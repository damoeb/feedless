package org.migor.feedless.cli

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.AppConfig
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.nio.charset.StandardCharsets

/** Serves install.sh with this instance's URL templated in, so an install always matches the instance's API. */
@Controller
@Profile("${AppProfiles.properties} & ${AppLayer.api}")
class CliInstallScriptController(
  private val appConfig: AppConfig,
  private val resourceLoader: ResourceLoader,
) {

  private val log = LoggerFactory.getLogger(CliInstallScriptController::class.simpleName)

  @Value("\${app.cli.installScriptLocation:file:./static/cli/install.sh}")
  private lateinit var installScriptLocation: String

  @GetMapping("/cli/install.sh")
  fun installScript(): ResponseEntity<String> {
    val resource = resourceLoader.getResource(installScriptLocation)
    if (!resource.exists()) {
      // Only the image's Go stage fills static/cli, so a local bootRun answers 404.
      log.debug("$installScriptLocation not found, feedctl was not cross-compiled into static/cli")
      return ResponseEntity.notFound().build()
    }

    val baseUrl = appConfig.apiGatewayUrl
    if (!FeedctlBaseUrlValidator.isValid(baseUrl)) {
      // An unvalidated baseUrl would run arbitrary shell on every curl | sh. It's operator config, so logging it is fine; never echo it.
      log.error("app.apiGatewayUrl='$baseUrl' is not a valid feedctl base URL; refusing to serve /cli/install.sh")
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .contentType(MediaType.TEXT_PLAIN)
        .body("feedctl install script is unavailable: this instance's apiGatewayUrl is misconfigured.")
    }

    val template = resource.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
    val script = template.replace(feedctlBaseUrlPlaceholder, baseUrl)

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, "text/x-shellscript")
      .body(script)
  }

  companion object {
    const val feedctlBaseUrlPlaceholder = "__FEEDCTL_BASE_URL__"
  }
}
