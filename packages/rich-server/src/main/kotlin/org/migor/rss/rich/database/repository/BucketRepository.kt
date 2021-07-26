package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.Bucket
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface BucketRepository : CrudRepository<Bucket, String> {
  @Query("""select b from Bucket b
    inner join ArticlePostProcessorToBucket pp2b on pp2b.id.bucketId = b.id
    order by b.lastPostProcessedAt asc""")
  fun findDueToPostProcessors(pageable: PageRequest): Iterable<Bucket>

}
