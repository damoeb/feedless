package org.migor.rich.rss.database.models

import org.hibernate.annotations.Type
import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.enums.GenericFeedStatus
import org.migor.rich.rss.transform.GenericFeedSpecification
import java.util.*
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "t_feed_generic")
open class GenericFeedEntity : EntityWithUUID() {

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var feedSpecification: GenericFeedSpecification

  @Basic
  @Column(nullable = false)
  open var feedSpecificationVersion: Int = 1

  @Basic
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: GenericFeedStatus = GenericFeedStatus.OK

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
  @JoinColumn(name = "managing_feed_id", referencedColumnName = "id")
  open var managingFeed: NativeFeedEntity? = null

  @Basic
  @Column(name = "managing_feed_id", nullable = false, insertable = false, updatable = false)
  open var managingFeedId: UUID? = null

//  @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
//  @JoinTable(
//    name = "map_generic_feed_to_tag",
//    joinColumns = [
//      JoinColumn(
//        name = "generic_feed_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )],
//    inverseJoinColumns = [
//      JoinColumn(
//        name = "tag_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )
//    ]
//  )
//  open var tags: List<TagEntity> = mutableListOf()

}
