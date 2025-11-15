package org.migor.feedless.community

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.migor.feedless.data.jpa.document.DocumentEntity

@Entity
@DiscriminatorValue("comment")
open class CommentEntity : DocumentEntity() {

  @Column(name = "is_original_poster")
  open var isOriginalPoster: Boolean = false
}
