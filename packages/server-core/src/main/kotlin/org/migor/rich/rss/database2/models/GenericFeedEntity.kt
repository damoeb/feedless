package org.migor.rich.rss.database2.models

import org.hibernate.annotations.Type
import org.migor.rich.rss.database2.EntityWithUUID
import org.migor.rich.rss.transform.ExtendedFeedRule
import java.sql.Timestamp
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Table


@Entity
@Table(name="t_generic_feed")
open class GenericFeedEntity: EntityWithUUID() {

//  @Basic
//  @Column(name = "home_page_url")
//  open var homePageUrl: String? = null
//
//  @Basic
//  @Column(name = "domain", nullable = false)
//  open var domain: String? = null

//  @Basic
//  @Column(name = "title")
//  open var title: String? = null

//    @Basic
//    @Column(name = "tags", nullable = true)
//    open var tags: Any? = null

  @Basic
  @Column(name = "description")
  open var description: String? = null

  @Type(type = "jsonb")
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var feedRule: ExtendedFeedRule

  @Basic
  @Column(name = "lastUpdatedAt")
  open var lastUpdatedAt: Timestamp? = null

  @Basic
  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var status: GenericFeedStatus

  @Basic
  @Column(name = "lastStatusChangeAt")
  open var lastStatusChangeAt: Timestamp? = null

}

enum class GenericFeedStatus {
  OK
}

