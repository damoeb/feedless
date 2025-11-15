package org.migor.feedless.jpa.source.actions

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.migor.feedless.jpa.document.DocumentEntity.Companion.LEN_STR_DEFAULT

@Entity
@Table(name = "t_action_execute_plugin")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class ExecuteActionEntity : ScrapeActionEntity() {

  @Size(message = "pluginId", max = LEN_STR_DEFAULT)
  @Column(name = "plugin_id", nullable = false)
  open lateinit var pluginId: String

  @Basic(fetch = FetchType.EAGER)
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb", name = "executor_params", nullable = false)
  open var executorParams: PluginExecutionJsonEntity? = null
}
