package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID
import java.util.*

@Entity
@Table(name = "map_bucket_to_article_refinement")
open class Bucket2RefinementEntity : EntityWithUUID() {

  @Basic
  @Column(name = "refinementId", nullable = false, insertable = false, updatable = false)
  open lateinit var refinementId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refinementId", referencedColumnName = "id")
  open var refinement: RefinementEntity? = null

  @Basic
  @Column(name = "bucketId", nullable = false, insertable = false, updatable = false)
  open lateinit var bucketId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bucketId", referencedColumnName = "id")
  open var bucket: BucketEntity? = null
}

