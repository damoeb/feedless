package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
import org.migor.feedless.generated.types.PuppeteerWaitUntil
import org.migor.feedless.generated.types.ScrapeAction
import org.migor.feedless.generated.types.ScrapeDebugOptions
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapePage
import org.migor.feedless.generated.types.ScrapePrerender
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.ViewPort
import java.util.*

@Entity
@Table(name = "t_scrape_source")
open class ScrapeSourceEntity : EntityWithUUID() {

  @Basic
  open var timeout: Int? = null

  @Column(nullable = false)
  open lateinit var url: String

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb")
  @Basic(fetch = FetchType.EAGER)
  open var viewport: ViewPort? = null

  @Basic
  open var language: String? = null

  @Basic
  @Column(nullable = false)
  open var prerender: Boolean = false

  @Basic
  @Enumerated(EnumType.STRING)
  open var waitUntil: PuppeteerWaitUntil? = null

  @Basic
  open var additionalWaitSec: Int? = null

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  open var actions: List<ScrapeAction>? = null

//  @OneToOne(fetch = FetchType.EAGER)
//  @JoinColumn(
//    name = "debugId",
//    referencedColumnName = "id",
//    insertable = false,
//    updatable = false,
//    nullable = false,
//    foreignKey = ForeignKey(name = "fk_scrape_source__debug")
//  )
//  open lateinit var debug: ScrapeDebugEntity

  @Column(nullable = false)
  open var debugScreenshot: Boolean = false

  @Column(nullable = false)
  open var debugConsole: Boolean = false

  @Column(nullable = false)
  open var debugNetwork: Boolean = false

  @Column(nullable = false)
  open var debugCookies: Boolean = false

  @Column(nullable = false)
  open var debugHtml: Boolean = false

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
    .errornous(erroneous)
    .lastErrorMessage(lastErrorMessage)
    .debug(
      ScrapeDebugOptions.newBuilder()
        .screenshot(debugScreenshot)
        .console(debugConsole)
        .network(debugNetwork)
        .cookies(debugCookies)
        .html(debugHtml)
        .build()
    )
    .page(
      ScrapePage.newBuilder()
        .prerender(
          if (prerender) {
            ScrapePrerender.newBuilder()
              .waitUntil(waitUntil)
              .viewport(viewport)
              .additionalWaitSec(additionalWaitSec ?: 0)
              .language(language)
              .build()
          } else {
            null
          }
        )
        .actions(actions)
        .timeout(timeout)
        .url(url)
        .build()
    )
    .emit(emit)
    .build()
}
