package org.migor.feedless.jpa.source.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Size

@Entity
@Table(name = "t_action_click_xpath")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class ClickXpathActionEntity : ScrapeActionEntity() {

  @XPathConstraint
  @Size(message = "xpath", min = 1, max = 100)
  @Column(name = "xpath", nullable = false)
  open lateinit var xpath: String
}
