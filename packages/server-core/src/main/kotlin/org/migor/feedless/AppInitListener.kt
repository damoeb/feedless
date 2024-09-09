package org.migor.feedless

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.util.*

@Service
class AppInitListener : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(AppInitListener::class.simpleName)

  @Value("\${APP_VERSION}")
  lateinit var version: String

  @Value("\${APP_GIT_HASH}")
  lateinit var commit: String

  @Value("\${app.timezone}")
  lateinit var tz: String

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

    System.out.println("Running v$version-$commit https://github.com/damoeb/feedless")
    TimeZone.setDefault(TimeZone.getTimeZone(tz))
    log.info("timezone=${tz} -> ${TimeZone.getDefault().id}")
  }

}
