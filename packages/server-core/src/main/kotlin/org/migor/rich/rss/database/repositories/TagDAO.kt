package org.migor.rich.rss.database.repositories

import org.migor.rich.rss.database.models.TagEntity
import org.migor.rich.rss.database.models.TagType
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TagDAO : CrudRepository<TagEntity, UUID> {
  fun findByNameAndType(name: String, type: TagType): TagEntity

//  @Query("""
//    select t from TagEntity t
//    inner join ArticleEntity A on A.tags
//    inner join Stream2ArticleEntity L on L.articleId = A.id
//    inner join NativeFeedEntity NF on NF.streamId = L.id
//    left join GenericFeedEntity GF on GF.managingFeedId = NF.id
//    where A.id = :articleId
//  """)
//  fun findByArticleId(@Param("articleId") articleId: UUID): List<TagEntity>
}
