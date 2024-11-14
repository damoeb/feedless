package org.migor.feedless.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import org.migor.feedless.document.DocumentEntity.Companion.LEN_STR_DEFAULT

@Entity
@Table(name = "t_action_header")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class HeaderActionEntity : ScrapeActionEntity() {

  @NotEmpty
  @Size(message = "name", max = LEN_STR_DEFAULT)
  @Column(name = "name", nullable = false)
  open lateinit var name: String

  @NotEmpty
  @Size(message = "value", max = LEN_STR_DEFAULT)
  @Column(name = "value", nullable = false)
  open lateinit var value: String
}
