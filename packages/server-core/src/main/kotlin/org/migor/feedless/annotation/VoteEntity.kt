package org.migor.feedless.annotation

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Entity
@Table(name = "t_annotation_vote")
@DiscriminatorValue("vote")
open class VoteEntity : AnnotationEntity() {
  @Column(nullable = false, name = "is_upvote")
  open var isUpVote: Boolean = false

  @Column(nullable = false, name = "is_downvote")
  open var isDownVote: Boolean = false

  @Column(nullable = false, name = "is_flag")
  open var isFlag: Boolean = false
}
