package org.migor.feedless.api.http

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.ResourceUtils
import org.springframework.web.bind.annotation.GetMapping
import java.nio.file.Files
import java.nio.file.Path

@Controller
@Profile(AppProfiles.serveStatic)
class ServeStaticController {

  @GetMapping("/")
  fun index(): ResponseEntity<String> {
    return ResponseEntity.ok(
      Files.readAllBytes(Path.of(ResourceUtils.toURI("file:${System.getProperty("user.dir")}/public/index.html")))
        .decodeToString()
    )
  }

}
