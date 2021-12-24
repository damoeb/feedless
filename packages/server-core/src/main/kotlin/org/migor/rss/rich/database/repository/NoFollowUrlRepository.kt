package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.NoFollowUrl
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NoFollowUrlRepository : CrudRepository<NoFollowUrl, String> {

  fun existsByUrlStartingWith(url: String): Boolean
}
