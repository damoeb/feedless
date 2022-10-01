package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

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

