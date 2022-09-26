package org.migor.rich.rss.database2.models

import org.hibernate.annotations.Type
import org.migor.rich.rss.database2.EntityWithUUID
import org.migor.rich.rss.transform.ExtendedFeedRule
import java.util.*
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
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
  @JoinColumn(name = "managing_feed_id", referencedColumnName = "id")
  open var managingFeed: NativeFeedEntity? = null

  @Basic
  @Column(name = "managing_feed_id", nullable = false, insertable = false, updatable = false)
  open var managingFeedId: UUID? = null


  @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
  @JoinTable(
    name = "map_feed_generic_to_tag",
    joinColumns = [
      JoinColumn(
        name = "generic_feed_id", referencedColumnName = "id",
        nullable = false, updatable = false
      )],
    inverseJoinColumns = [
      JoinColumn(
        name = "tag_id", referencedColumnName = "id",
        nullable = false, updatable = false
      )
    ]
  )
  open var tags: List<TagEntity> = mutableListOf()

}

enum class GenericFeedStatus {
  OK,
  NEED_APPROVAL
}

