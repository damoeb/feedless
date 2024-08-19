package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.migor.feedless.web.PuppeteerWaitUntil

@Entity
@Table(name = "t_action_fetch")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class FetchActionEntity : ScrapeActionEntity() {

  @Column(name = "timeout")
  open var timeout: Int? = null

  @Column(name = "url", nullable = false, length = 900)
  open lateinit var url: String

  @Column(name = "language")
  open var language: String? = null

  @Column(name = "force_prerender", nullable = false)
  open var forcePrerender: Boolean = false

  @Column(name = "is_variable", nullable = false)
  open var isVariable: Boolean = false

  @Column(name = "width", nullable = false)
  @Min(1024)
  @Max(2560)
  open var viewportWidth: Int = 1024

  @Column(name = "height", nullable = false)
  @Min(768)
  @Max(1440)
  open var viewportHeight: Int = 768

  @Column(name = "is_mobile", nullable = false)
  open var isMobile = false

  @Column(name = "is_landscape", nullable = false)
  open var isLandscape: Boolean = false

  @Enumerated(EnumType.STRING)
  @Column(name = "wait_until")
  open var waitUntil: PuppeteerWaitUntil? = null

  @Column(name = "additional_wait_sec")
  open var additionalWaitSec: Int? = null
}
