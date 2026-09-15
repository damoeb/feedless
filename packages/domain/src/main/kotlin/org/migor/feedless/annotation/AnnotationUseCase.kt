package org.migor.feedless.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentGuard
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryGuard
import org.migor.feedless.user.userId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

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

  private val log = org.slf4j.LoggerFactory.getLogger(AnnotationUseCase::class.simpleName)

  suspend fun createAnnotation(data: AnnotationCreate): Annotation {
    log.info("createAnnotation")

    return when (data) {
      is BoolAnnotationCreate -> createBoolAnnotation(data.target, data.flag, data.upVote, data.downVote)
      is TextAnnotationCreate -> createTextAnnotation(data.target, data.fromChar, data.toChar)
    }
  }

  suspend fun deleteAnnotation(id: AnnotationId) = withContext(Dispatchers.IO) {
    log.info("deleteAnnotation id=${id.uuid}")
    val annotation = annotationGuard.requireWrite(id)

    annotationRepository.deleteById(annotation.id)
  }

  private suspend fun createBoolAnnotation(
    target: AnnotationTarget,
    flag: Boolean = false,
    upvote: Boolean = false,
    downvote: Boolean = false,
  ): Annotation = withContext(Dispatchers.IO) {
    val (document, repository) = resolveReferences(target)

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
    target: AnnotationTarget,
    fromChar: Int,
    toChar: Int,
  ): Annotation = withContext(Dispatchers.IO) {
    val (document, repository) = resolveReferences(target)

    if (textAnnotationRepository.existsByFromCharAndToCharAndOwnerIdAndRepositoryIdAndDocumentId(
        fromChar,
        toChar,
        coroutineContext.userId(),
        document?.id,
        repository?.id
      )
    ) {
      throw IllegalArgumentException("duplicate")
    }

    val textAnnotation = TextAnnotation(
      fromChar = fromChar,
      toChar = toChar,
      repositoryId = repository?.id,
      documentId = document?.id,
      ownerId = coroutineContext.userId()
    )

    textAnnotationRepository.save(textAnnotation)
  }

  private suspend fun resolveReferences(
    target: AnnotationTarget,
  ): Pair<Document?, Repository?> {
    return Pair(
      target.documentId?.let { documentGuard.requireWrite(it) },
      target.repositoryId?.let { repositoryGuard.requireWrite(it) })
  }
}
