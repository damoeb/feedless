package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("header")
open class HeaderActionEntity : ScrapeActionEntity() {

  @Column(name = "header")
  open lateinit var xpath: String

  @Column(name = "data")
  open var data: String? = null
}
