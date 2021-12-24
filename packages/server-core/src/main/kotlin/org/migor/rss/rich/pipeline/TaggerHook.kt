package org.migor.rss.rich.pipeline

import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.database.model.TagNamespace
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.migor.rss.rich.util.JsonUtil
import org.springframework.stereotype.Service
import java.util.*

@Service
class TaggerHook : ArticleHook {
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
