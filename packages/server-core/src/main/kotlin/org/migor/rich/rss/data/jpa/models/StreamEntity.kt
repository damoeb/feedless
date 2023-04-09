package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID

@Entity
@Table(name = "t_stream")
open class StreamEntity : EntityWithUUID() {

  @OneToMany(mappedBy = "id", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  open var articles: MutableList<ArticleEntity>? = mutableListOf()
}

