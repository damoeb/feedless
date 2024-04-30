package org.migor.feedless.community

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("comment")
open class CommentEntity : PostEntity() {

  @Column(nullable = false, name = "is_original_poster")
  open var isOriginalPoster: Boolean = false
}
