package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.generated.types.ScrapeDebugOptions
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapePage
import org.migor.feedless.generated.types.ScrapeRequest
import java.util.*

@Entity
@Table(name = "t_scrape_source")
open class ScrapeSourceEntity : EntityWithUUID() {

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var page: ScrapePage

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  open var debug: ScrapeDebugOptions? = null

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var emit: List<ScrapeEmit>

  @Basic
  @Column(name = StandardJpaFields.subscriptionId, nullable = false)
  open lateinit var subscriptionId: UUID

  @Basic
  @Column(nullable = false)
  open var erroneous: Boolean = false

  @Basic
  open var lastErrorMessage: String? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.subscriptionId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_user__stream")
  )
  open var subscription: SourceSubscriptionEntity? = null
}

fun ScrapeSourceEntity.toDto(): ScrapeRequest {
  return ScrapeRequest.newBuilder()
    .id(id.toString())
    .debug(debug)
    .page(page)
    .emit(emit)
    .build()
}
