package org.migor.rich.rss.database.model

import org.springframework.context.annotation.Profile
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Profile("database")
@Entity
@Table(name = "\"_ArticlePostProcessorToBucket\"")
class ArticlePostProcessorToBucket() {
  constructor(id: ArticlePostProcessorToBucketId) : this() {
    this.id = id
  }

  @EmbeddedId
  var id: ArticlePostProcessorToBucketId? = null
}
