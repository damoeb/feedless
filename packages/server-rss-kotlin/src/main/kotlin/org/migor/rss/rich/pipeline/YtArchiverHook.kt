package org.migor.rss.rich.pipeline

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.Subscription
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.migor.rss.rich.transform.WebToFeedParser
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import javax.annotation.PostConstruct

@Service
class YtArchiverHook : ArticleHook {

  private val log = LoggerFactory.getLogger(WebToFeedParser::class.simpleName)

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
    ShellCommandHook.runCommand(
      corrId,
      "/usr/local/bin/youtube-dl -c -f best ${snapshot.article.url}",
      getTargetFolder(snapshot.subscription)
    )
    addTag(NamespacedTag(TagNamespace.CONTENT, "archived"))
    return true
  }

  private fun getTargetFolder(subscription: Subscription): File {
    val folder = StringUtils.trimToNull(URLEncoder.encode(StringUtils.abbreviate(subscription.name, 100), StandardCharsets.UTF_8))
    val actualFolder = File("${mount}/"+Optional.ofNullable(folder).orElse("default"))

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
