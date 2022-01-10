package org.migor.rich.rss.database.repository

import org.migor.rich.rss.database.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, String> {
  fun existsByEmail(email: String): Boolean
}
