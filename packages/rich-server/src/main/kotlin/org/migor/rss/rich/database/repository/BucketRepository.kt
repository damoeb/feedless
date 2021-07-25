package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Bucket
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BucketRepository : CrudRepository<Bucket, String>
