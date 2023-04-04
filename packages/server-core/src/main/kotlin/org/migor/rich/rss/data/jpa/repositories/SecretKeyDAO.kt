package org.migor.rich.rss.data.jpa.repositories

import org.migor.rich.rss.data.jpa.models.SecretKeyEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface SecretKeyDAO : JpaRepository<SecretKeyEntity, UUID> {

  @Query(
    """
    select K from SecretKeyEntity K
    inner join UserEntity U on U.id = K.ownerId
    where U.email = :email and U.locked = false and K.value = :value
  """
  )
  fun findBySecretKeyValue(@Param("value") secretKeyValue: String,
                           @Param("email") email: String): Optional<SecretKeyEntity>

  @Modifying
  @Transactional(propagation = Propagation.REQUIRED)
  @Query("""
    update SecretKeyEntity K SET K.lastUsedAt = :date where K.id = :id
  """)
  fun updateLastUsed(@Param("id") id: UUID, @Param("date") date: Date)
}
