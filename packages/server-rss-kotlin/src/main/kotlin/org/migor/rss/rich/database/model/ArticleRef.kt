package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.util.JsonUtil
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
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
@Table(name = "ArticleRef")
class ArticleRef {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "articleId")
  var articleId: String? = null

  @NotNull
  @Column(name = "ownerId")
  var ownerId: String? = null

  @Column(name = "tags", columnDefinition = "JSON")
  var tagsJson: String? = null

  @Transient
  var tags: List<NamespacedTag>? = null

  @Column(name = "data", columnDefinition = "JSON")
  var dataJson: String? = null

  @Transient
  var data: Map<String, String>? = null

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_released")
  var releasedAt: Date = Date()

  @PrePersist
  @PreUpdate
  fun prePersist() {
    tags?.let {
      tagsJson = JsonUtil.gson.toJson(tags)
    }
    data?.let {
      dataJson = JsonUtil.gson.toJson(data)
    }
  }

  @PostLoad
  fun postLoad() {
    tagsJson?.let {
      tags = JsonUtil.gson.fromJson<List<NamespacedTag>>(tagsJson, List::class.java)
    }
    dataJson?.let {
      data = JsonUtil.gson.fromJson<Map<String, String>>(dataJson, Map::class.java)
    }
  }
}
