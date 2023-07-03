package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.springframework.context.annotation.Profile
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
@Profile(AppProfiles.database)
interface WebDocumentDAO : JpaRepository<WebDocumentEntity, UUID>, PagingAndSortingRepository<WebDocumentEntity, UUID> {

  @Query(
    """
      select C from WebDocumentEntity C
        inner join ArticleEntity A on A.webDocumentId = C.id
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
  ): List<WebDocumentEntity>

  fun findByUrlOrAliasUrl(url: String, aliasUrl: String): WebDocumentEntity?

  @Modifying
  @Query(
    """
      update WebDocumentEntity C
        set C.contentTitle = :contentTitle,
            C.contentRaw = :contentRaw,
            C.contentRawMime = :contentRawMime,
            C.contentText = :contentText,
            C.imageUrl = :imageUrl,
            C.aliasUrl = :aliasUrl,
            C.hasFulltext = true,
            C.updatedAt = :now
      where C.id = :id
    """
  )
  fun saveFulltextContent(
      @Param("id") id: UUID,
      @Param("aliasUrl") aliasUrl: String?,
      @Param("contentTitle") contentTitle: String?,
      @Param("contentRaw") contentRaw: String?,
      @Param("contentRawMime") contentRawMime: String?,
      @Param("contentText") contentText: String?,
      @Param("imageUrl") imageUrl: String?,
      @Param("now") now: Date
  )

  @Modifying
  @Query(
    """
      update WebDocumentEntity C
        set C.contentRawMime = :contentRawMime,
            C.updatedAt = :now
      where C.id = :id
    """
  )
  fun saveContentRaw(
      @Param("id") id: UUID,
      @Param("contentRaw") contentRaw: String?,
      @Param("now") now: Date
  )

  @Query(
    value = """
      select C from WebDocumentEntity C
        inner join ArticleEntity A on A.webDocumentId = C.id
        inner join StreamEntity S on S.id = A.streamId
        inner join NativeFeedEntity F on F.streamId = S.id
        inner join ImporterEntity IMP on F.id = IMP.feedId
        where F.id in ?1 and A.createdAt >= ?2"""
  )
  fun findAllThrottled(
    feedId: UUID,
    articlesAfter: Date,
    pageable: PageRequest
  ): Stream<WebDocumentEntity>

  // todo should be A.updatedAt instead of S2A.createdAt
  @Query(
    """
      select C from WebDocumentEntity C
        inner join ArticleEntity A on A.webDocumentId = C.id
        inner join NativeFeedEntity F on F.streamId = A.streamId
        inner join ImporterEntity IMP on IMP.feedId = F.id
        where F.lastCheckedAt is not null
        and (
            (IMP.lastUpdatedAt is null and F.lastCheckedAt is not null)
            or
            (IMP.lastUpdatedAt < F.lastCheckedAt and A.createdAt > IMP.lastUpdatedAt)
        )
        and IMP.id = :importerId
        order by C.score desc, A.createdAt """
  )
  fun findNewArticlesForImporter(@Param("importerId") importerId: UUID): Stream<WebDocumentEntity>

  @Query(
    """
      select C from WebDocumentEntity C
        inner join ArticleEntity A on A.webDocumentId = C.id
        inner join NativeFeedEntity F on F.streamId = A.streamId
        inner join ImporterEntity IMP on IMP.feedId = F.id
        where F.lastCheckedAt is not null
        and (
            (IMP.lastUpdatedAt is null and C.releasedAt >= current_timestamp)
            or
            (IMP.lastUpdatedAt < F.lastCheckedAt and C.releasedAt >= IMP.lastUpdatedAt)
        )
        and IMP.id = :importerId
        and C.releasedAt < add_minutes(current_timestamp, :lookAheadMin)
        order by C.score desc, A.createdAt
    """
  )
  fun findArticlesForImporterWithLookAhead(
    @Param("importerId") importerId: UUID,
    @Param("lookAheadMin") lookAheadMin: Int
  ): Stream<WebDocumentEntity>

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  override fun deleteById(id: UUID)

  @Query(
    """
      select C from WebDocumentEntity C
      where C.finalized = false and (C.pluginsCoolDownUntil is null or C.pluginsCoolDownUntil < current_timestamp)
    """
  )
  fun findNextUnfinalized(pageable: PageRequest): List<WebDocumentEntity>

  @Modifying
  @Query(
    """
    DELETE FROM WebDocumentEntity d
    WHERE d.id in (
        select d1.id from WebDocumentEntity d1
        inner join ArticleEntity a1 on a1.webDocumentId = d1.id
        where a1.streamId = ?1
        order by a1.releasedAt desc
        offset ?2 ROWS
    )
    """)
  fun deleteAllByStreamIdWithSkip(streamId: UUID, skip: Int)

}
