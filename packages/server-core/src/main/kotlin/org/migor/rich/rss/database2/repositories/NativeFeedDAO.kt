package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.NativeFeedEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface NativeFeedDAO : CrudRepository<NativeFeedEntity, String>
