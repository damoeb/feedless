package org.migor.feedless.data.jpa.annotation

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.document.DocumentEntity
import org.migor.feedless.data.jpa.repository.RepositoryEntity
import org.migor.feedless.data.jpa.user.UserEntity
import java.util.*

@Entity
@Table(name = "t_annotation")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(
    name = "type",
    discriminatorType = DiscriminatorType.STRING
)
open class AnnotationEntity : EntityWithUUID() {

    @Column(name = "repository_id", nullable = true, unique = true)
    open var repositoryId: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(
        name = "repository_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_annotation__to__repository")
    )
    open var repository: RepositoryEntity? = null

    @Column(name = "document_id", nullable = true)
    open var documentId: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(
        name = "document_id",
        referencedColumnName = "id",
        insertable = false,
        updatable = false,
        foreignKey = ForeignKey(name = "fk_annotation__to__document")
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
        foreignKey = ForeignKey(name = "fk_annotation__to__user")
    )
    open var owner: UserEntity? = null
}

fun AnnotationEntity.toDomain(): org.migor.feedless.annotation.Annotation {
    return when (this) {
        is VoteEntity -> VoteMapper.INSTANCE.toDomain(this)
        is TextAnnotationEntity -> TextAnnotationMapper.INSTANCE.toDomain(this)
        else -> throw IllegalArgumentException("Unknown AnnotationEntity type: ${this.javaClass}")
    }
}

fun org.migor.feedless.annotation.Annotation.toEntity(): AnnotationEntity {
    return when (this) {
        is org.migor.feedless.annotation.Vote -> VoteMapper.INSTANCE.toEntity(this)
        is org.migor.feedless.annotation.TextAnnotation -> TextAnnotationMapper.INSTANCE.toEntity(this)
    }
}
