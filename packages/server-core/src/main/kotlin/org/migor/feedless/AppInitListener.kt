package org.migor.feedless

import org.migor.feedless.data.jpa.models.UserSecretEntity
import org.migor.feedless.data.jpa.models.UserSecretType
import org.migor.feedless.data.jpa.repositories.UserDAO
import org.migor.feedless.data.jpa.repositories.UserSecretDAO
import org.migor.feedless.service.PropertyService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class AppInitListener : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(AppInitListener::class.simpleName)

  @Value("\${CORE_VERSION}")
  lateinit var version: String

  @Value("\${OTHER_VERSIONS}")
  lateinit var otherVersion: Optional<String>

  @Value("\${GIT_HASH}")
  lateinit var hash: String

  @Autowired
  lateinit var userDAO: UserDAO

  @Autowired
  lateinit var userSecretDAO: UserSecretDAO

  @Autowired
  lateinit var propertyService: PropertyService

  override fun onApplicationEvent(event: ApplicationReadyEvent) {
    // http://www.patorjk.com/software/taag/#p=display&f=Shimrod&t=feedless
    System.out.println(
      """"
              . .
 ,-           | |
 |  ,-. ,-. ,-| | ,-. ,-. ,-.
 |- |-' |-' | | | |-' `-. `-.
 |  `-' `-' `-' ' `-' `-' `-'
-'

    """.trimIndent()
    )

    System.out.println("feedless:core v$version-$hash https://github.com/damoeb/feedless")
    otherVersion.ifPresent { System.out.println(it); }

    val root = userDAO.findRoot().orElseThrow { IllegalArgumentException("no root user found") }
    if (root.email != propertyService.rootEmail) {
      log.info("Updated rootEmail")
      root.email = propertyService.rootEmail
      userDAO.save(root)
    }

    userSecretDAO.findBySecretKeyValue(propertyService.rootSecretKey, propertyService.rootEmail).orElseGet {
      log.info("created secretKey for root")
      val userSecret = UserSecretEntity()
      userSecret.ownerId = root.id
      userSecret.value = propertyService.rootSecretKey
      userSecret.type = UserSecretType.SecretKey
      userSecret.validUntil =
        Date.from(LocalDateTime.now().plus(Duration.ofDays(356)).atZone(ZoneId.systemDefault()).toInstant())
      userSecretDAO.save(userSecret)
    }
  }

}
