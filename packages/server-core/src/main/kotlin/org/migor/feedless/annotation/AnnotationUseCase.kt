package org.migor.feedless.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.AnnotationWhereInput
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.TextAnnotationInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import org.migor.feedless.user.isAdmin
import org.migor.feedless.user.userId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*
import kotlin.coroutines.coroutineContext

@Service
@Profile("${AppProfiles.annotation} & ${AppLayer.service}")
class AnnotationUseCase(
  private val annotationRepository: AnnotationRepository,
  private val voteRepository: VoteRepository,
  private val textAnnotationRepository: TextAnnotationRepository
) {

  suspend fun createAnnotation(data: CreateAnnotationInput): Annotation = withContext(Dispatchers.IO) {
    val userId = coroutineContext.userId()
    data.annotation.flag?.let { createBoolAnnotation(data.where, flag = it.set, userId = userId) }
      ?: data.annotation.text?.let { createTextAnnotation(data.where, it, userId) }
      ?: data.annotation.upVote?.let { createBoolAnnotation(data.where, upvote = it.set, userId = userId) }
      ?: data.annotation.downVote?.let { createBoolAnnotation(data.where, downvote = it.set, userId = userId) }
      ?: throw IllegalArgumentException("Insufficient data for annotation")
  }

  suspend fun deleteAnnotation(data: DeleteAnnotationInput) = withContext(Dispatchers.IO) {
    val annotation = annotationRepository.findById(AnnotationId(data.where.id))
      ?: throw IllegalArgumentException("Annotation not found")
    if (coroutineContext.userId() == annotation.ownerId || isAdmin()) {
      annotationRepository.delete(annotation)
    } else {
      throw PermissionDeniedException("Must be owner")
    }
  }

  private suspend fun isAdmin(): Boolean {
    return coroutineContext.isAdmin()
  }

  private fun createBoolAnnotation(
    where: AnnotationWhereInput,
    flag: Boolean = false,
    upvote: Boolean = false,
    downvote: Boolean = false,
    userId: UserId
  ): Annotation {
    val (documentId, repositoryId) = resolveReferences(where)

    if (voteRepository.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        flag,
        upvote,
        downvote,
        userId,
        documentId,
        repositoryId
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    val vote = Vote(
      upVote = upvote,
      downVote = downvote,
      flag = flag,
      repositoryId = repositoryId,
      documentId = documentId,
      ownerId = userId,
    )

    return voteRepository.save(vote)
  }

  private fun createTextAnnotation(
    where: AnnotationWhereInput,
    i: TextAnnotationInput,
    userId: UserId
  ): Annotation {
    val (documentId, repositoryId) = resolveReferences(where)

    if (textAnnotationRepository.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        i.fromChar,
        i.toChar,
        userId,
        documentId,
        repositoryId
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    val textAnnotation = TextAnnotation(
      fromChar = i.fromChar,
      toChar = i.toChar,
      repositoryId = repositoryId,
      documentId = documentId,
      ownerId = userId
    )

    return textAnnotationRepository.save(textAnnotation)
  }

  private fun resolveReferences(
    where: AnnotationWhereInput,
  ): Pair<DocumentId?, RepositoryId?> {
    return Pair(
      where.document?.id?.let { DocumentId(UUID.fromString(it)) },
      where.repository?.id?.let { RepositoryId(UUID.fromString(it)) })
  }

//  suspend fun countUpVotesByRepositoryId(repositoryId: RepositoryId): Int = withContext(Dispatchers.IO) {
//    voteRepository.countUpVotesIsTrueByRepositoryId(repositoryId)
//  }
//
//  suspend fun countDownVotesByRepositoryId(repositoryId: RepositoryId): Int = withContext(Dispatchers.IO) {
//    voteRepository.countDownVoteIsTrueByRepositoryId(repositoryId)
//  }
//
//  suspend fun findAllVotesByUserIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote> =
//    withContext(Dispatchers.IO) {
//      voteRepository.findAllByOwnerIdAndRepositoryId(userId, repositoryId)
//    }
//
//  suspend fun findAllVotesByUserIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote> =
//    withContext(Dispatchers.IO) {
//      voteRepository.findAllByOwnerIdAndDocumentId(userId, documentId)
//    }

}
