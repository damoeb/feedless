package org.migor.feedless.annotation

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.ForeignKey
import jakarta.persistence.PrePersist
import jakarta.persistence.PrimaryKeyJoinColumn
import jakarta.persistence.Table
import org.migor.feedless.jpa.annotation.AnnotationEntity

@Entity
@Table(name = "t_annotation_vote")
@DiscriminatorValue("vote")
@PrimaryKeyJoinColumn(
  foreignKey = ForeignKey(
    name = "fk_annotation_entity",
    foreignKeyDefinition = "FOREIGN KEY (id) REFERENCES t_annotation(id) ON DELETE CASCADE"
  )
)

open class VoteEntity : AnnotationEntity() {
  @Column(nullable = false, name = "is_upvote")
  open var upVote: Boolean = false

//  @Column(nullable = false, name = "is_pinned")
//  open var pinned: Boolean = false

  @Column(nullable = false, name = "is_downvote")
  open var downVote: Boolean = false

  @Column(nullable = false, name = "is_flag")
  open var flag: Boolean = false

  @PrePersist
  fun prePersist() {
//    val trueValues = arrayOf(upVote, downVote, flag, pinned).filter { it }
    val trueValues = arrayOf(upVote, downVote, flag).filter { it }
    if (trueValues.size != 1) {
      throw IllegalArgumentException("invalid flags")
    }
  }
}
