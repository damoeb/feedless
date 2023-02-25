package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.migor.rich.rss.data.jpa.EntityWithUUID
import org.migor.rich.rss.data.jpa.enums.ArticleRefinementType

@Entity
@Table(name = "t_refinement")
open class RefinementEntity : EntityWithUUID() {

  @NotNull
  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  open var type: ArticleRefinementType? = null

  @Column
  open var context: String? = null

}

