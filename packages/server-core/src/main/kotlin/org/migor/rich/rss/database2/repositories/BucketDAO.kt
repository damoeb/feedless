package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.BucketEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BucketDAO : CrudRepository<BucketEntity, UUID>
