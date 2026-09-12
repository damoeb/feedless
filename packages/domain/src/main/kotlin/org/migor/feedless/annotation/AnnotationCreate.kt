package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId

data class AnnotationTarget(val documentId: DocumentId?, val repositoryId: RepositoryId?)

sealed interface AnnotationCreate {
  val target: AnnotationTarget
}

data class BoolAnnotationCreate(
  override val target: AnnotationTarget,
  val flag: Boolean = false,
  val upVote: Boolean = false,
  val downVote: Boolean = false,
) : AnnotationCreate

data class TextAnnotationCreate(
  override val target: AnnotationTarget,
  val fromChar: Int,
  val toChar: Int,
) : AnnotationCreate
