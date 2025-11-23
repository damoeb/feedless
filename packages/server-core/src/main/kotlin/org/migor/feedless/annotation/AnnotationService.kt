package org.migor.feedless.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.data.jpa.annotation.AnnotationDAO
import org.migor.feedless.data.jpa.annotation.AnnotationEntity
import org.migor.feedless.data.jpa.annotation.TextAnnotationDAO
import org.migor.feedless.data.jpa.annotation.TextAnnotationEntity
import org.migor.feedless.data.jpa.annotation.VoteDAO
import org.migor.feedless.data.jpa.annotation.VoteEntity
import org.migor.feedless.data.jpa.annotation.toDomain
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.AnnotationWhereInput
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.TextAnnotationInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.User
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.annotation} & ${AppLayer.service}")
class AnnotationService(
    val annotationDAO: AnnotationDAO,
    val voteDAO: VoteDAO,
    val textAnnotationDAO: TextAnnotationDAO
) {

    @Transactional
    suspend fun createAnnotation(data: CreateAnnotationInput, user: User): Annotation {
        return data.annotation.flag?.let { createBoolAnnotation(data.where, flag = it.set, user = user) }
            ?: data.annotation.text?.let { createTextAnnotation(data.where, it, user) }
            ?: data.annotation.upVote?.let { createBoolAnnotation(data.where, upvote = it.set, user = user) }
            ?: data.annotation.downVote?.let { createBoolAnnotation(data.where, downvote = it.set, user = user) }
            ?: throw IllegalArgumentException("Insufficient data for annotation")
    }

    @Transactional
    suspend fun deleteAnnotation(data: DeleteAnnotationInput, currentUser: User) {
        withContext(Dispatchers.IO) {
            val vote = annotationDAO.findById(UUID.fromString(data.where.id)).orElseThrow()
            if (currentUser.admin || currentUser.id == vote.ownerId) {
                annotationDAO.delete(vote)
            } else {
                throw PermissionDeniedException("Must be owner")
            }
        }
    }

    private fun createBoolAnnotation(
        where: AnnotationWhereInput,
        flag: Boolean = false,
        upvote: Boolean = false,
        downvote: Boolean = false,
        user: User
    ): Annotation {
        val v = VoteEntity()
        v.flag = flag
        v.upVote = upvote
        v.downVote = downvote
        v.ownerId = user.id.uuid

        val (documentId, repositoryId) = resolveReferences(where)
        linkDocumentOrRepository(v, documentId, repositoryId)

        if (voteDAO.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
                flag,
                upvote,
                downvote,
                user.id.uuid,
                documentId?.uuid,
                repositoryId?.uuid
            )
        ) {
            throw IllegalArgumentException("duplicate")
        }

        return voteDAO.save(v).toDomain()
    }

    private fun createTextAnnotation(
        where: AnnotationWhereInput,
        i: TextAnnotationInput,
        user: User
    ): Annotation {
        val t = TextAnnotationEntity()
        t.fromChar = i.fromChar
        t.toChar = i.toChar
        // todo extract actual text
        t.ownerId = user.id.uuid

        val (documentId, repositoyId) = resolveReferences(where)
        linkDocumentOrRepository(t, documentId, repositoyId)

        if (textAnnotationDAO.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
                i.fromChar,
                i.toChar,
                user.id.uuid,
                documentId?.uuid,
                repositoyId?.uuid
            )
        ) {
            throw IllegalArgumentException("duplicate")
        }

        return textAnnotationDAO.save(t).toDomain()
    }

    private fun linkDocumentOrRepository(
        a: AnnotationEntity,
        documentId: DocumentId?, repositoryId: RepositoryId?
    ) {
        if (repositoryId == null) {
            if (documentId == null) {
                throw IllegalArgumentException("where statement is insufficient")
            } else {
                a.documentId = documentId.uuid
            }
        } else {
            a.repositoryId = repositoryId.uuid
        }
    }

    private fun resolveReferences(
        where: AnnotationWhereInput,
    ): Pair<DocumentId?, RepositoryId?> {
        return Pair(
            where.document?.id?.let { DocumentId(UUID.fromString(it)) },
            where.repository?.id?.let { RepositoryId(UUID.fromString(it)) })
    }

    @Transactional(readOnly = true)
    suspend fun countUpVotesByRepositoryId(repositoryId: RepositoryId): Int {
        return withContext(Dispatchers.IO) {
            voteDAO.countUpVotesIsTrueByRepositoryId(repositoryId.uuid)
        }
    }

    @Transactional(readOnly = true)
    suspend fun countDownVotesByRepositoryId(repositoryId: RepositoryId): Int {
        return withContext(Dispatchers.IO) {
            voteDAO.countDownVoteIsTrueByRepositoryId(repositoryId.uuid)
        }
    }

    @Transactional(readOnly = true)
    suspend fun findAllVotesByUserIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote> {
        return withContext(Dispatchers.IO) {
            voteDAO.findAllByOwnerIdAndRepositoryId(userId.uuid, repositoryId.uuid).map { it.toDomain() }
        }
    }

    @Transactional(readOnly = true)
    suspend fun findAllVotesByUserIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote> {
        return withContext(Dispatchers.IO) {
            voteDAO.findAllByOwnerIdAndDocumentId(userId.uuid, documentId.uuid).map { it.toDomain() }
        }
    }

}
