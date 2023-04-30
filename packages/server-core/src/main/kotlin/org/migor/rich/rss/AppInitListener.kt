package org.migor.rich.rss

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
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

  override fun onApplicationEvent(event: ApplicationReadyEvent) {
    System.out.println(
      """"
   __   ______ _______ _______
 _|  |_|   __ \     __|     __|
|_    _|      <__     |__     |
  |__| |___|__|_______|_______|

    """.trimIndent()
    )

    System.out.println("richRSS v$version-$hash https://github.com/damoeb/rich-rss")
    otherVersion.ifPresent { System.out.println(it); }
  }

}
