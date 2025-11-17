package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime


data class Vote(
  val upVote: Boolean = false,
  val downVote: Boolean = false,
  val flag: Boolean = false,
  override val id: AnnotationId,
  override val repositoryId: RepositoryId,
  override val documentId: DocumentId,
  override val ownerId: UserId,
  override val createdAt: LocalDateTime
) : Annotation(id, repositoryId, documentId, ownerId, createdAt) {
}
