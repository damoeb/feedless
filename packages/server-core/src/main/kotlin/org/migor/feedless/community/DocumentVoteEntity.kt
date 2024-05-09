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
import org.migor.feedless.document.DocumentEntity
import org.migor.feedless.user.UserEntity
import java.util.*

@Entity
@Table(name = "t_document_vote")
open class DocumentVoteEntity : EntityWithUUID() {

  @Column(nullable = false, name = "is_upvote")
  open var isUpVote: Boolean = false

  @Column(nullable = false, name = "is_downvote")
  open var isDownVote: Boolean = false

  @Column(nullable = false, name = "is_flag")
  open var isFlag: Boolean = false

  @Column(name = "document_id", nullable = false)
  open lateinit var documentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "document_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var document: DocumentEntity? = null

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
