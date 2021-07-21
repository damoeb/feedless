package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.User
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : PagingAndSortingRepository<User, String> {
}
