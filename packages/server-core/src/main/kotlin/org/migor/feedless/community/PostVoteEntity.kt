package org.migor.feedless.community

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.user.UserEntity
import java.util.*

@Entity
@Table(name = "t_post_vote")
open class PostVoteEntity : EntityWithUUID() {

  @Column(nullable = false, name = "is_upvote")
  open var isUpVote: Boolean = false

  @Column(nullable = false, name = "is_downvote")
  open var isDownVote: Boolean = false

  @Column(nullable = false, name = "is_flag")
  open var isFlag: Boolean = false

  @Column(name = "post_id", nullable = false)
  open lateinit var postId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "post_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var post: PostEntity? = null

  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.ownerId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var owner: UserEntity? = null
}
