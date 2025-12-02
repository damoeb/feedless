package org.migor.feedless.data.jpa.group

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupId
import org.migor.feedless.group.GroupRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile("${AppProfiles.user} & ${AppLayer.repository}")
class GroupJpaRepository(private val groupDAO: GroupDAO) : GroupRepository {
  override fun findByName(name: String): Group? {
    return groupDAO.findByName(name)?.toDomain()
  }

  override fun findById(groupId: GroupId): Group? {
    return groupDAO.findById(groupId.uuid).getOrNull()?.toDomain()
  }

  override fun save(group: Group): Group {
    return groupDAO.save(group.toEntity()).toDomain()
  }
}
