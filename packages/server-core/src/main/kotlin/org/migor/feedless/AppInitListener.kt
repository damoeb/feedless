package org.migor.feedless

import org.springframework.context.annotation.Profile
import org.migor.feedless.status.BuildInfo
import org.migor.feedless.common.PublicUrls
import org.migor.feedless.common.LocaleProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.properties)
class AppInitListener(
  private val buildInfo: BuildInfo,
  private val localeProperties: LocaleProperties,
  private val publicUrls: PublicUrls,
) : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(AppInitListener::class.simpleName)

  override fun onApplicationEvent(event: ApplicationReadyEvent) {
    // http://www.patorjk.com/software/taag/#p=display&f=Shimrod&t=feedless
    println(
      """"
              . .
 ,-           | |
 |  ,-. ,-. ,-| | ,-. ,-. ,-.
 |- |-' |-' | | | |-' `-. `-.
 |  `-' `-' `-' ' `-' `-' `-'
-'

    """.trimIndent()
    )

    System.out.println("Running v${buildInfo.version}-${buildInfo.commit} https://github.com/damoeb/feedless")
    TimeZone.setDefault(TimeZone.getTimeZone(localeProperties.timezone))
    log.info("timezone=${localeProperties.timezone} -> ${TimeZone.getDefault().id}")
    log.info("apiGatewayUrl=${publicUrls.apiGatewayUrl} appHost=${publicUrls.appHost}")
  }

}
