package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleHookType
import org.migor.rich.rss.database.model.ArticleHookSpec
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.database.model.TagNamespace
import org.migor.rich.rss.harvest.ArticleSnapshot
import org.migor.rich.rss.util.JsonUtil
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaggerHook : PipelineHook {
  override fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: Bucket,
    hookSpec: ArticleHookSpec,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean {

    Optional.ofNullable(hookSpec.context)
      .ifPresent {
        JsonUtil.gson.fromJson(it, Array<String>::class.java)
          .forEach { tag ->
            addTag(
              NamespacedTag(TagNamespace.USER, tag)
            )
          }
      }

    return true
  }

  override fun type(): ArticleHookType = ArticleHookType.tag

}
