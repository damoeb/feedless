package org.migor.feedless.data.jpa.source.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.migor.feedless.actions.DomEventType
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT

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
  @Size(message = "xpath", min = 1, max = 100)
  @Column(name = "xpath", nullable = false)
  open lateinit var xpath: String

  @Column(length = 50, name = "event", nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var event: DomEventType

  @Size(message = "data", max = LEN_STR_DEFAULT)
  @Column(name = "data")
  open var data: String? = null
}

fun DomActionEntity.toDomain(): org.migor.feedless.actions.DomAction {
  return DomActionMapper.Companion.INSTANCE.toDomain(this)
}
