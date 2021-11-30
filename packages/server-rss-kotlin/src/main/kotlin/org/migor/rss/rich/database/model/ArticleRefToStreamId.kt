package org.migor.rss.rich.database.model

import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class ArticleRefToStreamId() : Serializable {
  constructor(articleRefId: String?, streamId: String?) : this() {
    this.articleRefId = articleRefId
    this.streamId = streamId
  }

  @Column(name = "\"A\"")
  var articleRefId: String? = null

  @Column(name = "\"B\"")
  var streamId: String? = null
}
