package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, String> {
  fun existsByEmail(email: String): Boolean
}
