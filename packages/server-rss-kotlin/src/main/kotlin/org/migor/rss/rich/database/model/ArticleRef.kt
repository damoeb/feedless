package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.util.JsonUtil
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.*
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

  @NotNull
  @Column(name = "tags", columnDefinition = "JSON")
  var tagsJson: String? = null

  @Transient
  var tags: List<NamespacedTag>? = null

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
  }

  @PostLoad
  fun postLoad() {
    tagsJson?.let {
      tags = JsonUtil.gson.fromJson<List<NamespacedTag>>(tagsJson, List::class.java)
    }
  }
}
