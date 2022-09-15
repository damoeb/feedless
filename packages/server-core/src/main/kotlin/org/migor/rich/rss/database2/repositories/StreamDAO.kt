package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.StreamEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface StreamDAO : CrudRepository<StreamEntity, String>
