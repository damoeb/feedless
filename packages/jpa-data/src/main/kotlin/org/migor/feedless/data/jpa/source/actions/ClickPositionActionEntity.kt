package org.migor.feedless.data.jpa.source.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

@Entity
@Table(name = "t_action_click_position")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class ClickPositionActionEntity : ScrapeActionEntity() {

  @Column(name = "x", nullable = false)
  @Min(0)
  @Max(10000)
  open var x: Int = 0

  @Column(name = "y", nullable = false)
  @Min(0)
  @Max(10000)
  open var y: Int = 0
}
