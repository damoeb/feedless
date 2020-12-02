package org.migor.rss.rich.repositories

import org.migor.rss.rich.models.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CrudRepository<User, String>
