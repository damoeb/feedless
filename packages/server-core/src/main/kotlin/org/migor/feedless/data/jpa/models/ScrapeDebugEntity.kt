package org.migor.feedless.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID

@Entity
@Table(name = "t_scrape_debug")
open class ScrapeDebugEntity : EntityWithUUID() {

  @Column(nullable = false)
  open val screenshot: Boolean = false

  @Column(nullable = false)
  open val console: Boolean = false

  @Column(nullable = false)
  open val network: Boolean = false

  @Column(nullable = false)
  open val cookies: Boolean = false

  @Column(nullable = false)
  open val html: Boolean = false

}
