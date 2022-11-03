package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "t_stream")
open class StreamEntity : EntityWithUUID() {

  @OneToMany(mappedBy = "id", fetch = FetchType.LAZY)
  open var articles: MutableList<ArticleEntity>? = mutableListOf()

}

