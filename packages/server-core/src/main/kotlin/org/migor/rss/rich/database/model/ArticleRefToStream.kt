package org.migor.rss.rich.database.model

import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "\"_ArticleRefToStream\"")
class ArticleRefToStream() {
  constructor(id: ArticleRefToStreamId) : this() {
    this.id = id
  }

  @EmbeddedId
  var id: ArticleRefToStreamId? = null
}
