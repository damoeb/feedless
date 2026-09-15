package org.migor.feedless.data.jpa.userSecret

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Repository
@Profile("${AppProfiles.secrets} & ${AppLayer.repository}")
interface UserSecretDAO : JpaRepository<UserSecretEntity, UUID> {

  @Query(
    """
    select K from UserSecretEntity K
    inner join UserEntity U on U.id = K.ownerId
    where U.email = :email and U.locked = false and K.value = :value
  """
  )
  fun findBySecretKeyValue(
    @Param("value") secretKeyValue: String,
    @Param("email") email: String
  ): UserSecretEntity?

  fun existsByValueAndOwnerId(value: String, ownerId: UUID): Boolean

  @Modifying
  @Query(
    """
    update UserSecretEntity K SET K.lastUsedAt = :date where K.id = :id
  """
  )
  @Transactional
  fun updateLastUsed(@Param("id") id: UUID, @Param("date") date: LocalDateTime)

  // The WHERE clause throttles: every API request calls this, but a fresh timestamp is left alone.
  @Modifying
  @Query(
    """
    update UserSecretEntity K SET K.lastUsedAt = :date
    where K.id = :id and (K.lastUsedAt is null or K.lastUsedAt < :staleBefore)
  """
  )
  @Transactional
  fun updateLastUsedIfStale(
    @Param("id") id: UUID,
    @Param("date") date: LocalDateTime,
    @Param("staleBefore") staleBefore: LocalDateTime
  )

  fun findAllByOwnerId(id: UUID): List<UserSecretEntity>

}
