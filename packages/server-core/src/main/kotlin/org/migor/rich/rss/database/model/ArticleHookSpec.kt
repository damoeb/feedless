package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rich.rss.database.enums.ArticleHookType
import org.springframework.context.annotation.Profile
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull

@Profile("stateful")
@Entity
@Table(name = "\"ArticlePostProcessor\"")
class ArticleHookSpec {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  var type: ArticleHookType? = null

  @Column(name = "context")
  var context: String? = null

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"createdAt\"")
  var createdAt: Date = Date()
}
