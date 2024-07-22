package org.migor.feedless.repository

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Profile(AppProfiles.database)
interface RepositoryCronRunDAO : JpaRepository<RepositoryCronRunEntity, UUID> {
  fun findAllByRepositoryIdOrderByCreatedAtDesc(id: UUID): List<RepositoryCronRunEntity>

}
