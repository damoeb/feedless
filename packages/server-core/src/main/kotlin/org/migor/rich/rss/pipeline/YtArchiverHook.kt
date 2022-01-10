package org.migor.rich.rss.pipeline

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.database.enums.ArticleHookType
import org.migor.rich.rss.database.model.ArticleHookSpec
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.database.model.Subscription
import org.migor.rich.rss.database.model.TagNamespace
import org.migor.rich.rss.harvest.ArticleSnapshot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.annotation.PostConstruct

@Service
class YtArchiverHook : ArticleHook {

  private val log = LoggerFactory.getLogger(YtArchiverHook::class.simpleName)

  //  private val mount = "./mount"
  private val mount = "/home/damoeb/videos/matteo"

  @PostConstruct
  fun postConstruct() {
    log.info("Using base folder ${File(mount).absolutePath}")
  }

  override fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: Bucket,
    hookSpec: ArticleHookSpec,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean {
    val targetFolder = getTargetFolder(snapshot.subscription)
    this.log.info("[${corrId}] Archiving to $targetFolder")
    ShellCommandHook.runCommand(
      corrId,
      "/usr/local/bin/youtube-dl -c -f best ${snapshot.article.url}",
      targetFolder
    )
    addTag(NamespacedTag(TagNamespace.CONTENT, "archived"))
    return true
  }

  private fun getTargetFolder(subscription: Subscription): File {
    val folder =
      StringUtils.trimToNull(URLEncoder.encode(StringUtils.abbreviate(subscription.name, 100), StandardCharsets.UTF_8))
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

  override fun type(): ArticleHookType = ArticleHookType.yt

}
