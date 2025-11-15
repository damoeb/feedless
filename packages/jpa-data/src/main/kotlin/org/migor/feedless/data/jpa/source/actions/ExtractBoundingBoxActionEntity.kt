package org.migor.feedless.data.jpa.source.actions

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.migor.feedless.data.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT

@Entity
@Table(name = "t_action_extract_bbox")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class ExtractBoundingBoxActionEntity : ScrapeActionEntity() {

  @Column(name = "x", nullable = false)
  @Min(0)
  @Max(10000)
  open var x: Int = 0

  @Size(message = "fragmentName", max = LEN_STR_DEFAULT)
  @Column(name = "fragment_name", nullable = false)
  open lateinit var fragmentName: String

  @Column(name = "y", nullable = false)
  @Min(0)
  @Max(10000)
  open var y: Int = 0

  @Column(name = "w", nullable = false)
  @Min(0)
  @Max(10000)
  open var w: Int = 0

  @Column(name = "h", nullable = false)
  @Min(0)
  @Max(10000)
  open var h: Int = 0
}
