package org.migor.rich.rss.database2.models

import org.hibernate.annotations.Type
import org.migor.rich.rss.database2.EntityWithUUID
import org.migor.rich.rss.transform.ExtendedFeedRule
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table


@Entity
@Table(name = "t_feed_generic")
open class GenericFeedEntity : EntityWithUUID() {

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var feedRule: ExtendedFeedRule

  @Basic
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: GenericFeedStatus? = GenericFeedStatus.OK

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "managingFeedId", referencedColumnName = "id")
  open var managingFeed: NativeFeedEntity? = null

  @Basic
  @Column(name = "managingFeedId", nullable = false, insertable = false, updatable = false)
  open var managingFeedId: UUID? = null

}

enum class GenericFeedStatus {
  OK,
  NEED_APPROVAL
}

