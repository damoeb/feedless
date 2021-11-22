package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.database.enums.ExporterTargetType
import org.migor.rss.rich.util.JsonUtil
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.PostLoad
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Table
import javax.persistence.Transient
import javax.validation.constraints.NotNull

@Entity
@Table(name = "ArticleExporterTarget")
class ExporterTarget {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  var type: ExporterTargetType? = null

  @Column(name = "forward_errors")
  var forwardErrors: Boolean = false

  @NotNull
  @Column(name = "context", columnDefinition = "JSON")
  var contextJson: String? = null

  @Transient
  var context: Map<String, Any>? = null

  @Column(name = "exporterId")
  var exporterId: String? = null

  @PrePersist
  @PreUpdate
  fun prePersist() {
    context?.let {
      contextJson = JsonUtil.gson.toJson(context)
    }
  }

  @PostLoad
  fun postLoad() {
    contextJson?.let {
      context = JsonUtil.gson.fromJson<Map<String, Any>>(contextJson, Map::class.java)
    }
  }
}
