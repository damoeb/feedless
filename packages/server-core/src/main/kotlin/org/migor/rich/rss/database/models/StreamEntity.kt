package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "t_stream")
open class StreamEntity : EntityWithUUID() {

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feedId", referencedColumnName = "id")
  open var feed: NativeFeedEntity? = null

  @OneToMany(mappedBy = "id", fetch = FetchType.LAZY)
  open var articles: MutableList<Stream2ArticleEntity>? = mutableListOf()

}

