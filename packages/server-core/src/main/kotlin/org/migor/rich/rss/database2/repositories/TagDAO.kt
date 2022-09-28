package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.TagEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TagDAO : CrudRepository<TagEntity, UUID>
