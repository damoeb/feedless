package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.enums.ArticleSource
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.springframework.data.domain.PageRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

@Repository
interface ContentDAO : JpaRepository<ContentEntity, UUID>, PagingAndSortingRepository<ContentEntity, UUID> {

  @Query(
    """
      select C from ContentEntity C
        inner join ArticleEntity A on A.contentId = C.id
        where A.streamId = ?1
            and A.type = ?2
            and A.status = ?3
    """
  )
  fun findAllByStreamId(
      streamId: UUID,
      type: ArticleType,
      status: ReleaseStatus,
      pageable: PageRequest
  ): List<ContentEntity>

//  @Query(
//    """select C from ContentEntity C
//        inner join ArticleEntity A on A.contentId = C.id
//        where C.id = :id and A.streamId = :streamId
//    """
//  )
//  fun findInStream(@Param("id") articleId: UUID, @Param("streamId") streamId: UUID): Optional<ContentEntity>
//
//  @Transactional(readOnly = true)
//  fun findByUrl(url: String): Optional<ContentEntity>

  fun findByUrl(url: String): ContentEntity?

  @Modifying
  @Query(
    """
      update ContentEntity C
        set C.contentTitle = :contentTitle,
            C.contentRaw = :contentRaw,
            C.contentRawMime = :contentRawMime,
            C.contentSource = :contentSource,
            C.contentText = :contentText,
            C.imageUrl = :imageUrl,
            C.url = :url,
            C.hasFulltext = true,
            C.updatedAt = :now
      where C.id = :id
    """
  )
  fun saveFulltextContent(
      @Param("id") id: UUID,
      @Param("url") url: String,
      @Param("contentTitle") contentTitle: String?,
      @Param("contentRaw") contentRaw: String?,
      @Param("contentRawMime") contentRawMime: String?,
      @Param("contentSource") contentSource: ArticleSource,
      @Param("contentText") contentText: String?,
      @Param("imageUrl") imageUrl: String?,
      @Param("now") now: Date
  )

  @Query(
    value = """
      select C from ContentEntity C
        inner join ArticleEntity A on A.contentId = C.id
        inner join StreamEntity S on S.id = A.streamId
        inner join NativeFeedEntity F on F.streamId = S.id
        inner join ImporterEntity IMP on F.id = IMP.feedId
        where F.id in ?1 and A.createdAt >= ?2"""
  )
  fun findAllThrottled(
    feedId: UUID,
    articlesAfter: Date,
    pageable: PageRequest
  ): Stream<ContentEntity>

  // todo should be A.updatedAt instead of S2A.createdAt
  @Query(
    """
      select C from ContentEntity C
        inner join ArticleEntity A on A.contentId = C.id
        inner join NativeFeedEntity F on F.streamId = A.streamId
        inner join ImporterEntity IMP on IMP.feedId = F.id
        where F.lastUpdatedAt is not null
        and (
            (IMP.lastUpdatedAt is null and F.lastUpdatedAt is not null)
            or
            (IMP.lastUpdatedAt < F.lastUpdatedAt and A.createdAt > IMP.lastUpdatedAt)
        )
        and IMP.id = :importerId
        order by C.score desc, A.createdAt """
  )
  fun findNewArticlesForImporter(@Param("importerId") importerId: UUID): Stream<ContentEntity>

  @Query(
    """
      select C from ContentEntity C
        inner join ArticleEntity A on A.contentId = C.id
        inner join NativeFeedEntity F on F.streamId = A.streamId
        inner join ImporterEntity IMP on IMP.feedId = F.id
        where F.lastUpdatedAt is not null
        and (
            (IMP.lastUpdatedAt is null and C.releasedAt >= current_timestamp)
            or
            (IMP.lastUpdatedAt < F.lastUpdatedAt and C.releasedAt >= IMP.lastUpdatedAt)
        )
        and IMP.id = :importerId
        and C.releasedAt < add_minutes(current_timestamp, :lookAheadMin)
        order by C.score desc, A.createdAt
    """
  )
  fun findArticlesForImporterWithLookAhead(
    @Param("importerId") importerId: UUID,
    @Param("lookAheadMin") lookAheadMin: Int
  ): Stream<ContentEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  override fun deleteById(id: UUID)
}
