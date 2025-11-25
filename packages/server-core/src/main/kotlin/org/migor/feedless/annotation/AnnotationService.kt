package org.migor.feedless.annotation

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
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
  val annotationRepository: AnnotationRepository,
  val voteRepository: VoteRepository,
  val textAnnotationRepository: TextAnnotationRepository
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
    val annotation = annotationRepository.findById(AnnotationId(data.where.id))
      ?: throw IllegalArgumentException("Annotation not found")
    if (currentUser.admin || currentUser.id == annotation.ownerId) {
      annotationRepository.delete(annotation)
    } else {
      throw PermissionDeniedException("Must be owner")
    }
  }

  private suspend fun createBoolAnnotation(
    where: AnnotationWhereInput,
    flag: Boolean = false,
    upvote: Boolean = false,
    downvote: Boolean = false,
    user: User
  ): Annotation {
    val (documentId, repositoryId) = resolveReferences(where)

    if (voteRepository.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        flag,
        upvote,
        downvote,
        user.id,
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
      ownerId = user.id,
    )

    return voteRepository.save(vote)
  }

  private suspend fun createTextAnnotation(
    where: AnnotationWhereInput,
    i: TextAnnotationInput,
    user: User
  ): Annotation {
    val (documentId, repositoryId) = resolveReferences(where)

    if (textAnnotationRepository.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        i.fromChar,
        i.toChar,
        user.id,
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
      ownerId = user.id
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

  @Transactional(readOnly = true)
  suspend fun countUpVotesByRepositoryId(repositoryId: RepositoryId): Int {
    return voteRepository.countUpVotesIsTrueByRepositoryId(repositoryId)
  }

  @Transactional(readOnly = true)
  suspend fun countDownVotesByRepositoryId(repositoryId: RepositoryId): Int {
    return voteRepository.countDownVoteIsTrueByRepositoryId(repositoryId)
  }

  @Transactional(readOnly = true)
  suspend fun findAllVotesByUserIdAndRepositoryId(userId: UserId, repositoryId: RepositoryId): List<Vote> {
    return voteRepository.findAllByOwnerIdAndRepositoryId(userId, repositoryId)
  }

  @Transactional(readOnly = true)
  suspend fun findAllVotesByUserIdAndDocumentId(userId: UserId, documentId: DocumentId): List<Vote> {
    return voteRepository.findAllByOwnerIdAndDocumentId(userId, documentId)
  }

}
