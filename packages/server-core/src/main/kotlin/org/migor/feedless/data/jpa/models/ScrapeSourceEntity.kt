package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.ScrapeRequest
import java.util.*

@Entity
@Table(name = "t_scrape_source")
open class ScrapeSourceEntity : EntityWithUUID() {

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var scrapeRequest: ScrapeRequest

  @Basic
  @Column(name = StandardJpaFields.subscriptionId, nullable = false)
  open lateinit var subscriptionId: UUID

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
  @JoinColumn(name = StandardJpaFields.subscriptionId, referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_user__stream"))
  open var subscription: SourceSubscriptionEntity? = null

//  @Basic
//  @Column(name = StandardJpaFields.sourceSubscriptionId, nullable = false)
//  open lateinit var subscriptionId: UUID
//
//
//  @ManyToOne(fetch = FetchType.LAZY, cascade = [])
//  @JoinColumn(name = StandardJpaFields.sourceSubscriptionId, referencedColumnName = "id", insertable = false, updatable = false,
//    foreignKey = ForeignKey(name = "fk_subscription__scrape_source")
//  )
//  open var subscription: SourceSubscriptionEntity? = null
}

