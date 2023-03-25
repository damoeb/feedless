package org.migor.rich.rss.pipeline

import jakarta.annotation.PostConstruct
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.ContentWithContext
import org.migor.rich.rss.data.jpa.enums.ArticleRefinementType
import org.migor.rich.rss.data.jpa.models.ImporterEntity
import org.migor.rich.rss.data.jpa.models.RefinementEntity
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

@Profile(AppProfiles.database)
@Service
class YtArchiverHook : PreImportAction {

  private val log = LoggerFactory.getLogger(YtArchiverHook::class.simpleName)

    private val mount = "./mount"

  @PostConstruct
  fun postConstruct() {
    log.info("Using base folder ${File(mount).absolutePath}")
  }

  override fun process(
      corrId: String,
      snapshot: ContentWithContext,
      refinement: RefinementEntity,
  ): Boolean {
    val targetFolder = getTargetFolder(snapshot.importer)
    this.log.info("[${corrId}] Archiving to $targetFolder")
    ShellCommandHook.runCommand(
      corrId,
      "/usr/local/bin/youtube-dl -c -f best ${snapshot.content.url}",
      targetFolder
    )
    return true
  }

  private fun getTargetFolder(importer: ImporterEntity): File {
    val folder =
      StringUtils.trimToNull(
        URLEncoder.encode(
          StringUtils.abbreviate(importer.bucket!!.title, 100),
          StandardCharsets.UTF_8
        )
      )
    val actualFolder = File("${mount}/" + Optional.ofNullable(folder).orElse("default"))

    if (actualFolder.exists()) {
      if (!actualFolder.isDirectory) {
        throw RuntimeException("folder ${actualFolder.absolutePath} must be a directory")
      }
      if (!actualFolder.canWrite()) {
        throw RuntimeException("folder ${actualFolder.absolutePath} must be writeable")
      }
    } else {
      if (!actualFolder.mkdirs()) {
        throw RuntimeException("folder ${actualFolder.absolutePath} cannot be created")
      }
    }

    return actualFolder
  }

  override fun type(): ArticleRefinementType = ArticleRefinementType.yt

}
