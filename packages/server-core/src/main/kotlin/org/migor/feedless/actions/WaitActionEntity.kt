package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Size

@Entity
@Table(name = "t_action_wait")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class WaitActionEntity : ScrapeActionEntity() {

  @XPathConstraint
  @Size(min = 1)
  @Column(name = "xpath", nullable = false)
  open lateinit var xpath: String
}
