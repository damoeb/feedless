package org.migor.rich.rss.database.repository

import org.migor.rich.rss.database.model.ArticleHookSpec
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArticlePostProcessorRepository : CrudRepository<ArticleHookSpec, String> {

  @Query(
    """select pp from ArticleHookSpec pp
    inner join ArticlePostProcessorToBucket pp2b on pp2b.id.articlePostProcessor = pp.id
    where pp2b.id.bucketId = :bucketId"""
  )
  fun findAllByBucketId(@Param("bucketId") bucketId: String): List<ArticleHookSpec>
}
