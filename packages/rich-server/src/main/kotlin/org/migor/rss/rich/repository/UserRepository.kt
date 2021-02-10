package org.migor.rss.rich.repository

import org.migor.rss.rich.model.User
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : PagingAndSortingRepository<User, String> {
  fun findByEmailHash(emvailHash: String): User
}
