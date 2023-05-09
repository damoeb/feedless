package org.migor.feedless.data.jpa.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID

@Entity
@Table(name = "t_stream")
open class StreamEntity : EntityWithUUID() {

  @OneToMany(mappedBy = "id", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  open var articles: MutableList<ArticleEntity>? = mutableListOf()

  @OneToOne(mappedBy = "stream", cascade = [], orphanRemoval = false, optional = true)
  open var bucket: BucketEntity? = null

}

