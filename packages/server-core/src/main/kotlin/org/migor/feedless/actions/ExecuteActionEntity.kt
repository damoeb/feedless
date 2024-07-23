package org.migor.feedless.actions

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.migor.feedless.generated.types.PluginExecutionParamsInput

@Entity
@Table(name = "t_action_execute_plugin")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_base_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_scrape_action(id) ON DELETE CASCADE"
  )
)
open class ExecuteActionEntity : ScrapeActionEntity() {

  @Column(name = "plugin_id", nullable = false)
  open lateinit var pluginId: String

  @Basic(fetch = FetchType.EAGER)
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb", name = "executor_params", nullable = false)
  open var executorParams: PluginExecutionParamsInput? = null
}
