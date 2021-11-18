package org.migor.rss.rich.plugins

import org.migor.rss.rich.database.enums.PostProcessorType
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.springframework.stereotype.Service
import java.io.File

data class YtArchiverOptions(val format: String, val workingDir: String)

@Service
class YtArchiverPlugin : PostProcessorPlugin<YtArchiverOptions> {
  override fun getType(): PostProcessorType = PostProcessorType.YT
  override fun getDefaultOptions(): YtArchiverOptions = YtArchiverOptions("best", "")
  override fun process(article: Article, bucket: Bucket, options: Map<String,String>?) {
    ShellCommandPlugin.runCommand("/usr/local/bin/youtube-dl -f hls-1221-0 ${article.url}", File("/tmp"))
  }

}
