package org.migor.feedless.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.generated.types.AnnotationWhereInput
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.TextAnnotationInput
import org.migor.feedless.user.UserEntity
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.annotation} & ${AppLayer.service}")
class AnnotationService(
  val annotationDAO: AnnotationDAO,
  val voteDAO: VoteDAO,
  val textAnnotationDAO: TextAnnotationDAO
) {

  suspend fun createAnnotation(corrId: String, data: CreateAnnotationInput, user: UserEntity): AnnotationEntity {
    return data.annotation.flag?.let { createBoolAnnotation(corrId, data.where, flag = it.set, user = user) }
      ?: data.annotation.text?.let { createTextAnnotation(corrId, data.where, it, user) }
      ?: data.annotation.upVote?.let { createBoolAnnotation(corrId, data.where, upvote = it.set, user = user) }
      ?: data.annotation.downVote?.let { createBoolAnnotation(corrId, data.where, downvote = it.set, user = user) }
      ?: throw IllegalArgumentException("Insufficient data for annotation")
  }

  suspend fun deleteAnnotation(corrId: String, data: DeleteAnnotationInput, currentUser: UserEntity) {
    withContext(Dispatchers.IO) {
      val vote = annotationDAO.findById(UUID.fromString(data.where.id)).orElseThrow()
      if (currentUser.root || currentUser.id == vote.ownerId) {
        annotationDAO.delete(vote)
      } else {
        throw PermissionDeniedException("Must be owner")
      }
    }
  }

  private fun createBoolAnnotation(
    corrId: String,
    where: AnnotationWhereInput,
    flag: Boolean = false,
    upvote: Boolean = false,
    downvote: Boolean = false,
    user: UserEntity
  ): AnnotationEntity {
    val v = VoteEntity()
    v.flag = flag
    v.upVote = upvote
    v.downVote = downvote
    v.ownerId = user.id

    val (documentId, repositoyId) = resolveReferences(where)
    linkDocumentOrRepository(v, documentId, repositoyId)

    if (voteDAO.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        flag,
        upvote,
        downvote,
        user.id,
        documentId,
        repositoyId
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    return voteDAO.save(v)
  }

  private fun createTextAnnotation(
    corrId: String,
    where: AnnotationWhereInput,
    i: TextAnnotationInput,
    user: UserEntity
  ): AnnotationEntity {
    val t = TextAnnotationEntity()
    t.fromChar = i.fromChar
    t.toChar = i.toChar
    // todo extract actual text
    t.ownerId = user.id

    val (documentId, repositoyId) = resolveReferences(where)
    linkDocumentOrRepository(t, documentId, repositoyId)

    if (textAnnotationDAO.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        i.fromChar,
        i.toChar,
        user.id,
        documentId,
        repositoyId
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    return textAnnotationDAO.save(t)
  }

  private fun linkDocumentOrRepository(
    a: AnnotationEntity,
    documentId: UUID?, repositoryId: UUID?
  ) {
    if (repositoryId == null) {
      if (documentId == null) {
        throw IllegalArgumentException("where statement is insufficient")
      } else {
        a.documentId = documentId
      }
    } else {
      a.repositoryId = repositoryId
    }
  }

  private fun resolveReferences(
    where: AnnotationWhereInput,
  ): Pair<UUID?, UUID?> {
    return Pair(where.document?.id?.let { UUID.fromString(it) }, where.repository?.id?.let { UUID.fromString(it) })
  }

  suspend fun countUpVotesByRepositoryId(repositoryId: UUID): Int {
    return withContext(Dispatchers.IO) {
      voteDAO.countUpVotesIsTrueByRepositoryId(repositoryId)
    }
  }

  suspend fun countDownVotesByRepositoryId(repositoryId: UUID): Int {
    return withContext(Dispatchers.IO) {
      voteDAO.countDownVoteIsTrueByRepositoryId(repositoryId)
    }
  }

  suspend fun findAllVotesByUserIdAndRepositoryId(userId: UUID, repositoryId: UUID): List<VoteEntity> {
    return withContext(Dispatchers.IO) {
      voteDAO.findAllByOwnerIdAndRepositoryId(userId, repositoryId)
    }
  }

}
