package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Size

@Entity
@Table(name = "t_action_dom")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class DomActionEntity : ScrapeActionEntity() {

  @XPathConstraint
  @Size(min = 1, max = 100)
  @Column(name = "xpath", nullable = false)
  open lateinit var xpath: String

  @Column(length = 50, name = "event", nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var event: DomEventType

  @Column(name = "data")
  open var data: String? = null
}

enum class DomEventType {
  purge,
  type,
  select,
}
