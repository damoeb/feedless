package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Stream
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StreamRepository : CrudRepository<Stream, String>
