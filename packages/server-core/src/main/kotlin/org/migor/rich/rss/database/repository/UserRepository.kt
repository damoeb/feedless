package org.migor.rich.rss.database.repository

import org.migor.rich.rss.database.model.User
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("database")
interface UserRepository : CrudRepository<User, String> {
  fun existsByEmail(email: String): Boolean
}
