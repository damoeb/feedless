package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
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
  var tags: String? = null

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date = Date()

}
