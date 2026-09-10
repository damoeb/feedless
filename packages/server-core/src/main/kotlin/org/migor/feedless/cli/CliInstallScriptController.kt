package org.migor.feedless.cli

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ResourceLoader
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import java.nio.charset.StandardCharsets

/**
 * feedctl (packages/cli) is cross-compiled and baked into every self-hosted
 * instance's image (see packages/cli/build.gradle.kts crossCompile and
 * packages/server-core/Dockerfile), so a `curl .../cli/install.sh | sh`
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
      // No local run of `:packages:cli:crossCompile` has populated
      // static/cli yet -- 404, rather than failing bootRun (see
      // packages/cli/README.md).
      log.debug("$installScriptLocation not found, feedctl was not cross-compiled into static/cli")
      return ResponseEntity.notFound().build()
    }

    val template = resource.inputStream.use { it.readBytes().toString(StandardCharsets.UTF_8) }
    val script = template.replace(feedctlBaseUrlPlaceholder, propertyService.apiGatewayUrl)

    return ResponseEntity.ok()
      .header(HttpHeaders.CONTENT_TYPE, "text/x-shellscript")
      .body(script)
  }

  companion object {
    const val feedctlBaseUrlPlaceholder = "__FEEDCTL_BASE_URL__"
  }
}
