package org.migor.feedless.data.jpa.repositories

import org.migor.feedless.AppProfiles
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

  fun findAllBySubscriptionIdAndStatus(
    subscriptionId: UUID,
    status: ReleaseStatus,
    pageable: PageRequest
  ): List<WebDocumentEntity>

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

  @Modifying
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  override fun deleteById(id: UUID)

  @Query(
    """
      select C from WebDocumentEntity C
      where C.finalized = false and (C.pluginsCoolDownUntil is null or C.pluginsCoolDownUntil < current_timestamp)
    """
  )
  fun findNextUnfinalized(pageable: PageRequest): List<WebDocumentEntity>

  @Query(
    """
    SELECT D FROM WebDocumentEntity D
    WHERE (D.url = :url or D.aliasUrl = :url) and D.subscriptionId = :subscriptionId
    """)
  fun findByUrlAndSubscriptionId(@Param("url") url: String, @Param("subscriptionId") subscriptionId: UUID): WebDocumentEntity?


  fun existsByContentTitleAndSubscriptionId(title: String, subscriptionId: UUID): Boolean
}
