package org.migor.feedless.annotation

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.PermissionDeniedException
import org.migor.feedless.guard.ResourceGuard
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.isAdmin
import org.migor.feedless.user.userId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.annotation} & ${AppLayer.service}")
class AnnotationGuard(
  private val userRepository: UserRepository,
  private val repositoryRepository: RepositoryRepository,
  private val annotationRepository: AnnotationRepository
) : ResourceGuard<AnnotationId, Annotation> {

  override suspend fun requireRead(id: AnnotationId): Annotation {
    TODO("Not yet implemented")
  }

  override suspend fun requireWrite(id: AnnotationId): Annotation {
    TODO("Not yet implemented")
  }

  override suspend fun requireExecute(id: AnnotationId): Annotation = withContext(Dispatchers.IO) {
    // todo requireRead(userId)
    val annotation = annotationRepository.findById(id)
      ?: throw IllegalArgumentException("Annotation not found")

    if (coroutineContext.userId() != annotation.ownerId && !coroutineContext.isAdmin()) {
      throw PermissionDeniedException("Must be owner")
    }
    annotation
  }
}
