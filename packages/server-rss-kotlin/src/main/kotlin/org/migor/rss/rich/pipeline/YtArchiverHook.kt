package org.migor.rss.rich.pipeline

import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.springframework.stereotype.Service
import java.io.File

@Service
class YtArchiverHook : ArticleHook {
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
      File("/home/damoeb/videos/matteo")
    )
    addTag(NamespacedTag(TagNamespace.CONTENT, "archived"))
    return true
  }

  override fun type(): ArticleHookType = ArticleHookType.yt

}
