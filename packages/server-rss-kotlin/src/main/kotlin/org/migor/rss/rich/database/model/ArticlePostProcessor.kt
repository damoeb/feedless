package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.database.enums.ArticlePostProcessorType
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.*
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
  var type: ArticlePostProcessorType = ArticlePostProcessorType.FOLLOW_LINKS

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date = Date()
}
