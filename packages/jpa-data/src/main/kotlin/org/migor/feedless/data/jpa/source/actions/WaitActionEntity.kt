package org.migor.feedless.data.jpa.source.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT

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
  @Size(message = "xpath", min = 1, max = LEN_STR_DEFAULT)
  @Column(name = "xpath", nullable = false)
  open lateinit var xpath: String
}
