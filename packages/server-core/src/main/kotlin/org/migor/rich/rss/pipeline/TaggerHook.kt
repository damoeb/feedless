package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleRefinementType
import org.migor.rich.rss.database.enums.NamespacedTag
import org.migor.rich.rss.database.enums.TagNamespace
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.RefinementEntity
import org.migor.rich.rss.harvest.ArticleSnapshot
import org.migor.rich.rss.util.JsonUtil
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Profile("database2")
@Service
class TaggerHook : PipelineHook {
  override fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: BucketEntity,
    hookSpec: RefinementEntity,
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

  override fun type(): ArticleRefinementType = ArticleRefinementType.tag

}
