package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull

@Entity
@Table(name = "\"ArticleRef\"")
class ArticleRef: JsonSupport() {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "\"articleId\"")
  var articleId: String? = null

  @NotNull
  @Column(name = "\"streamId\"")
  lateinit var streamId: String

  @NotNull
  @Column(name = "\"ownerId\"")
  var ownerId: String? = null

  @Column(name = "tags", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var tags: List<NamespacedTag>? = null

  @Column(name = "data", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var data: Map<String, String>? = null

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"createdAt\"")
  var createdAt: Date = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "date_released")
  var releasedAt: Date = Date()
}
