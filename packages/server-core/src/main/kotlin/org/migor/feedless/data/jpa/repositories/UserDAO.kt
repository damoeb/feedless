package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.data.jpa.models.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserDAO : JpaRepository<UserEntity, UUID> {
  fun findByEmail(name: String): Optional<UserEntity>
  fun existsByEmail(email: String): Boolean
  fun findByName(name: String): Optional<UserEntity>
}
