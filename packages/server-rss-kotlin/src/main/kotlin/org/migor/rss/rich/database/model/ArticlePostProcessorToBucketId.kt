package org.migor.rss.rich.database.model

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class ArticlePostProcessorToBucketId() : Serializable {
  constructor(articlePostProcessor: String?, bucketId: String?) : this() {
    this.articlePostProcessor = articlePostProcessor
    this.bucketId = bucketId
  }

  @Column(name = "\"A\"")
  var articlePostProcessor: String? = null

  @Column(name = "\"B\"")
  var bucketId: String? = null
}
