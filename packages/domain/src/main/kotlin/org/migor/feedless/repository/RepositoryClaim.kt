package org.migor.feedless.repository

import java.time.LocalDateTime

data class RepositoryClaim(
  val id: RepositoryClaimId = RepositoryClaimId(),
  val repositoryId: RepositoryId? = null,
  val createdAt: LocalDateTime = LocalDateTime.now(),
)
