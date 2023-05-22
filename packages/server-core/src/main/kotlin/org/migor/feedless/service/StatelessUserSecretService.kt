package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.repositories.UserSecretDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
@Profile("!${AppProfiles.database}")
class StatelessUserSecretService: UserSecretService {

  private val log = LoggerFactory.getLogger(StatelessUserSecretService::class.simpleName)

  override fun findBySecretKeyValue(secretKeyValue: String, email: String): Optional<UserSecretEntity> {
    TODO()
  }

  override fun updateLastUsed(id: UUID, date: Date) {
    // nothing
  }

}
