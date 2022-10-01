package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.StreamEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StreamDAO : CrudRepository<StreamEntity, UUID>
