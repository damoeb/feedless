package org.migor.feedless.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentGuard
import org.migor.feedless.document.DocumentId
import org.migor.feedless.generated.types.AnnotationWhereInput
import org.migor.feedless.generated.types.CreateAnnotationInput
import org.migor.feedless.generated.types.DeleteAnnotationInput
import org.migor.feedless.generated.types.TextAnnotationInput
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.userId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.annotation} & ${AppLayer.service}")
class AnnotationUseCase(
  private val annotationRepository: AnnotationRepository,
  private val voteRepository: VoteRepository,
  private val textAnnotationRepository: TextAnnotationRepository,
  private val annotationGuard: AnnotationGuard,
  private val documentGuard: DocumentGuard,
  private val repositoryGuard: RepositoryGuard
) {

  suspend fun createAnnotation(data: CreateAnnotationInput): Annotation {

    return data.annotation.flag?.let { createBoolAnnotation(data.where, flag = it.set) }
      ?: data.annotation.text?.let { createTextAnnotation(data.where, it) }
      ?: data.annotation.upVote?.let { createBoolAnnotation(data.where, upvote = it.set) }
      ?: data.annotation.downVote?.let { createBoolAnnotation(data.where, downvote = it.set) }
      ?: throw IllegalArgumentException("Insufficient data for annotation")
  }

  suspend fun deleteAnnotation(data: DeleteAnnotationInput) = withContext(Dispatchers.IO) {

    val annotation = annotationGuard.requireWrite(AnnotationId(data.where.id))

    annotationRepository.deleteById(annotation.id)
  }

  private suspend fun createBoolAnnotation(
    where: AnnotationWhereInput,
    flag: Boolean = false,
    upvote: Boolean = false,
    downvote: Boolean = false,
  ): Annotation = withContext(Dispatchers.IO) {
    val (document, repository) = resolveReferences(where)

    if (voteRepository.existsByFlagAndUpVoteAndDownVoteAndOwnerIdAndRepositoryIdAndDocumentId(
        flag,
        upvote,
        downvote,
        coroutineContext.userId(),
        document?.id,
        repository?.id
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    val vote = Vote(
      upVote = upvote,
      downVote = downvote,
      flag = flag,
      repositoryId = repository?.id,
      documentId = document?.id,
      ownerId = coroutineContext.userId(),
    )

    voteRepository.save(vote)
  }

  private suspend fun createTextAnnotation(
    where: AnnotationWhereInput,
    i: TextAnnotationInput,
  ): Annotation = withContext(Dispatchers.IO) {
    val (document, repository) = resolveReferences(where)

    if (textAnnotationRepository.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        i.fromChar,
        i.toChar,
        coroutineContext.userId(),
        document?.id,
        repository?.id
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    val textAnnotation = TextAnnotation(
      fromChar = i.fromChar,
      toChar = i.toChar,
      repositoryId = repository?.id,
      documentId = document?.id,
      ownerId = coroutineContext.userId()
    )

    textAnnotationRepository.save(textAnnotation)
  }

  private suspend fun resolveReferences(
    where: AnnotationWhereInput,
  ): Pair<Document?, Repository?> {
    return Pair(
      where.document?.id?.let { documentGuard.requireWrite(DocumentId(UUID.fromString(it))) },
      where.repository?.id?.let { repositoryGuard.requireWrite(RepositoryId(UUID.fromString(it))) })
  }
}
