package org.migor.feedless.cli

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
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

/**
 * feedctl (packages/cli) is cross-compiled and baked into every self-hosted
 * instance's image by the Go stage of packages/server-core/Dockerfile, so a
 * `curl .../cli/install.sh | sh`
 * against any instance always installs the CLI build matching that
 * instance's API. The binaries and SHA256SUMS are plain static files served
 * from the CLI static location (`/cli/`, all paths); only install.sh needs
 * templating, to bake in this instance's own public URL.
 */
@Controller
@Profile("${AppProfiles.properties} & ${AppLayer.api}")
class CliInstallScriptController(
  private val propertyService: PropertyService,
  private val resourceLoader: ResourceLoader,
) {

  private val log = LoggerFactory.getLogger(CliInstallScriptController::class.simpleName)

  @Value("\${app.cli.installScriptLocation:file:./static/cli/install.sh}")
  private lateinit var installScriptLocation: String

  @GetMapping("/cli/install.sh")
  fun installScript(): ResponseEntity<String> {
    val resource = resourceLoader.getResource(installScriptLocation)
    if (!resource.exists()) {
      // Only the image's Go stage populates static/cli, so a local bootRun
      // has none -- 404, rather than failing bootRun (see
      // packages/cli/README.md).
      log.debug("$installScriptLocation not found, feedctl was not cross-compiled into static/cli")
      return ResponseEntity.notFound().build()
    }

    val baseUrl = propertyService.apiGatewayUrl
    if (!FeedctlBaseUrlValidator.isValid(baseUrl)) {
      // baseUrl is templated straight into a double-quoted shell assignment
      // in install.sh; serving it unvalidated would let a misconfigured
      // apiGatewayUrl run arbitrary shell on every `curl | sh` install. Log
      // the actual value (it's an operator config value logged at startup
      // by PropertyService already, not attacker input) but never echo it
      // into the response.
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
