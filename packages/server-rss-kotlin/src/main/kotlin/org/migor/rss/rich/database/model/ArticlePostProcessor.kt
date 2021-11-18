package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.database.enums.PostProcessorType
import org.migor.rss.rich.util.JsonUtil
import org.springframework.data.annotation.CreatedDate
import java.util.*
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
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.persistence.Transient
import javax.validation.constraints.NotNull

@Entity
@Table(name = "ArticlePostProcessor")
class ArticlePostProcessor {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  var type: PostProcessorType? = null

  @Column(name = "context", columnDefinition = "JSON")
  var contextJson: String? = null

  @Transient
  var context: Map<String,String>? = null

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date = Date()

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
      context = JsonUtil.gson.fromJson<Map<String,String>>(contextJson, Map::class.java)
    }
  }

}
