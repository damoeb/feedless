package org.migor.rich.rss.service

import org.migor.rich.rss.data.jpa.models.SecretKeyEntity
import org.migor.rich.rss.data.jpa.models.UserEntity
import org.migor.rich.rss.data.jpa.repositories.SecretKeyDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class SecretKeyService {

  private val log = LoggerFactory.getLogger(SecretKeyService::class.simpleName)

  @Autowired
  lateinit var secretKeyDAO: SecretKeyDAO

  fun createSecretKey(secretKey: String, expiresIn: Duration, user: UserEntity): SecretKeyEntity {
    val k = SecretKeyEntity()
    k.ownerId = user.id
    k.value = secretKey
    k.validUntil = Date.from(LocalDateTime.now().plus(expiresIn).atZone(ZoneId.systemDefault()).toInstant())

    return secretKeyDAO.save(k)
  }

}
