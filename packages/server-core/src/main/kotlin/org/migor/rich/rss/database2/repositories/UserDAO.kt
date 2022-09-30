package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserDAO : CrudRepository<UserEntity, UUID> {
  fun findByName(name: String): UserEntity?
  fun existsByEmail(email: String): Boolean
}
