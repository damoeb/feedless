package org.migor.rich.rss.database.model

import org.springframework.context.annotation.Profile
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Profile("database")
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
