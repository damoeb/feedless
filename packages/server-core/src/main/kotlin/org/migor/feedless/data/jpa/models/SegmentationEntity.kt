package org.migor.feedless.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.generated.types.Segment

@Entity
@Table(name = "t_segment")
open class SegmentationEntity : EntityWithUUID() {

  @Column(nullable = false, name = "digest")
  open var digest: Boolean = false

  @Column(nullable = false, name = "size")
  @Min(1)
  open var size: Int = 0

  @Column(nullable = false, name = "sort_by")
  open lateinit var sortBy: String

  @Column(nullable = false, name = "sort_Asc")
  open var sortAsc: Boolean = false

  @OneToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  open var repository: RepositoryEntity? = null
}

fun SegmentationEntity.toDto(): Segment {
  return Segment.newBuilder()
    .digest(digest)
    .size(size)
    .sortAsc(sortAsc)
    .sortBy(sortBy)
    .build()
}
