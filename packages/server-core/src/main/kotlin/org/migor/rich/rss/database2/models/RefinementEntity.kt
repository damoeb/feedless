package org.migor.rich.rss.database2.models

import org.migor.rich.rss.database2.enums.ArticleRefinementType
import org.migor.rich.rss.database2.EntityWithUUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "t_refinement")
open class RefinementEntity : EntityWithUUID() {

  @NotNull
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  open var type: ArticleRefinementType? = null

  @Column(name = "context")
  open var context: String? = null

}

