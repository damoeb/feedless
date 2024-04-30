package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Entity
@DiscriminatorValue("dom")
open class DomActionEntity : ScrapeActionEntity() {

  @Column(name = "xpath")
  open lateinit var xpath: String

  @Column(length = 50, name = "event")
  @Enumerated(EnumType.STRING)
  open lateinit var event: DomEventType

  @Column(name = "data")
  open var data: String? = null
}

enum class DomEventType {
  click,
  purge,
  type,
  select,
  wait
}
