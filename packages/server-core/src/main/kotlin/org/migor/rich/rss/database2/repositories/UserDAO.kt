package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserDAO : CrudRepository<UserEntity, String> {
  fun findByName(name: String): UserEntity?
}
