package org.migor.feedless.data.jpa.oneTimePassword

import org.migor.feedless.AppLayer
import org.migor.feedless.oneTimePassword.OneTimePasswordRepository
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.otp.OneTimePasswordId
import org.migor.feedless.user.UserId
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Component
@Transactional(propagation = Propagation.MANDATORY)
@Profile(AppLayer.repository)
class OneTimePasswordJpaRepository(private val oneTimePasswordDAO: OneTimePasswordDAO) : OneTimePasswordRepository {
  override fun deleteAllByValidUntilBefore(now: LocalDateTime) {
    oneTimePasswordDAO.deleteAllByValidUntilBefore(now)
  }

  override fun findFirstByUserIdOrderByCreatedAtDesc(userId: UserId): OneTimePassword? {
    return oneTimePasswordDAO.findFirstByUserIdOrderByCreatedAtDesc(userId.uuid)?.toDomain()
  }

  override fun findById(id: OneTimePasswordId): OneTimePassword? {
    return oneTimePasswordDAO.findById(id.uuid).getOrNull()?.toDomain()
  }

  override fun save(otp: OneTimePassword): OneTimePassword {
    return oneTimePasswordDAO.save(otp.toEntity()).toDomain()
  }

  override fun deleteById(id: OneTimePasswordId) {
    oneTimePasswordDAO.deleteById(id.uuid)
  }
}
