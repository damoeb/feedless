package org.migor.feedless.data.jpa.group

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository
import kotlin.jvm.optionals.getOrNull

@Repository
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class GroupJpaRepository(private val groupDAO: GroupDAO) : GroupRepository {
  override suspend fun findByName(name: String): Group? {
    return withContext(Dispatchers.IO) {
      groupDAO.findByName(name)?.toDomain()
    }
  }

  override suspend fun findById(groupId: GroupId): Group? {
    return withContext(Dispatchers.IO) {
      groupDAO.findById(groupId.uuid).getOrNull()?.toDomain()
    }
  }

  override suspend fun save(group: Group): Group {
    return withContext(Dispatchers.IO) {
      groupDAO.save(group.toEntity()).toDomain()
    }
  }
}
