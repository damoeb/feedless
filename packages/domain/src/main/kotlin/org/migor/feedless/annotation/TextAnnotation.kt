package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime


data class TextAnnotation(
  val fromChar: Int,
  val toChar: Int,
  override val id: AnnotationId = AnnotationId(),
  override val repositoryId: RepositoryId?,
  override val documentId: DocumentId?,
  override val ownerId: UserId,
  override val createdAt: LocalDateTime = LocalDateTime.now(),
) : Annotation(id, repositoryId, documentId, ownerId, createdAt) {
}
