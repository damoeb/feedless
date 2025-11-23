package org.migor.feedless.secrets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ResumableHarvestException
import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordDAO
import org.migor.feedless.data.jpa.oneTimePassword.OneTimePasswordEntity
import org.migor.feedless.data.jpa.oneTimePassword.toDomain
import org.migor.feedless.data.jpa.oneTimePassword.toEntity
import org.migor.feedless.otp.OneTimePassword
import org.migor.feedless.user.User
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime


@Service
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.security} & ${AppLayer.service}")
class OneTimePasswordService(
    private val oneTimePasswordDAO: OneTimePasswordDAO
) {

    private val log = LoggerFactory.getLogger(OneTimePasswordService::class.simpleName)

    private val otpValidForMinutes: Long = 5
    private val otpConfirmCodeLength: Int = 5

    @Transactional
    suspend fun createOTP(user: User): OneTimePassword {
        val otp = createOTP()
            .copy(
                userId = user.id
            )
        withContext(Dispatchers.IO) {
            oneTimePasswordDAO.findFirstByUserIdOrderByCreatedAtDesc(user.id.uuid)?.let {
                val now = LocalDateTime.now()
                if (it.createdAt.isAfter(now.minusSeconds(30))) {
                    throw ResumableHarvestException("Token cooldown", Duration.between(it.createdAt, now))
                }
            }
            oneTimePasswordDAO.save(otp.toEntity())
        }
        log.debug("sending otp '${otp.password}'")
        return otp
    }

    suspend fun createOTP(): OneTimePassword {
        val otp = OneTimePasswordEntity()
        otp.password = CryptUtil.newCorrId(otpConfirmCodeLength).uppercase()
        otp.validUntil = LocalDateTime.now().plusMinutes(otpValidForMinutes)
        return otp.toDomain()
    }

    @Transactional
    fun deleteAllByValidUntilBefore(now: LocalDateTime) {
        oneTimePasswordDAO.deleteAllByValidUntilBefore(now)
    }

}
