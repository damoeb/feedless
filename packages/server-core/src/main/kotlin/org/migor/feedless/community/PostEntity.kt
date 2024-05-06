package org.migor.feedless.community

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.models.DocumentEntity
import org.migor.feedless.user.UserEntity
import java.util.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "t_post")
@DiscriminatorColumn(
  name = "type",
  discriminatorType = DiscriminatorType.STRING
)
open class PostEntity : EntityWithUUID() {

  @Column(nullable = false, length = 1000, name = "content")
  open lateinit var content: String

  @Column(nullable = false, name = "is_dead")
  open var dead: Boolean = false

  @Column(nullable = false, name = "is_flagged")
  open var flagged: Boolean = false

  @Column(nullable = false, name = "base_score")
  open var baseScore: Double = 0.0

  @Column(nullable = false, name = "is_controversial")
  open var controversial: Boolean = false

  @Column(name = "parent_id", nullable = false)
  open lateinit var parentId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "parent_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
  )
  open var parent: PostEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var votes: MutableList<PostVoteEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var children: MutableList<PostEntity> = mutableListOf()

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
