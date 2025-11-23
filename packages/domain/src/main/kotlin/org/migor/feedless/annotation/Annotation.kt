package org.migor.feedless.annotation

import org.migor.feedless.document.DocumentId
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.user.UserId
import java.time.LocalDateTime
import java.util.*

sealed class Annotation(
    open val id: AnnotationId = AnnotationId(UUID.randomUUID()),
    open val repositoryId: RepositoryId?,
    open val documentId: DocumentId?,
    open val ownerId: UserId,
    open val createdAt: LocalDateTime = LocalDateTime.now(),
)

