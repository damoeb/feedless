package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.UserSecretEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface UserSecretDAO : JpaRepository<UserSecretEntity, UUID> {

  @Query(
    """
    select K from UserSecretEntity K
    inner join UserEntity U on U.id = K.ownerId
    where U.email = :email and U.locked = false and K.value = :value
  """
  )
  fun findBySecretKeyValue(@Param("value") secretKeyValue: String,
                           @Param("email") email: String): Optional<UserSecretEntity>

  @Modifying
  @Transactional(propagation = Propagation.REQUIRED)
  @Query(
    """
    update UserSecretEntity K SET K.lastUsedAt = :date where K.id = :id
  """
  )
  fun updateLastUsed(@Param("id") id: UUID, @Param("date") date: Date)

  fun findByOwnerId(id: UUID): List<UserSecretEntity>

  @Modifying
  @Query(
    """
    delete from UserSecretEntity K where K.id in (:ids) and K.ownerId = :ownerId
  """
  )
  fun deleteAllByIdAndOwnerId(ids: List<UUID>, ownerId: UUID)
}
