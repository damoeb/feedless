package org.migor.rich.rss

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.migor.rich.rss.service.HttpService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.util.*

@Service
class AppInitListener : ApplicationListener<ApplicationReadyEvent> {

  private val log = LoggerFactory.getLogger(AppInitListener::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var environment: Environment

  @Value("\${app.masterUrl}")
  lateinit var masterUrl: String

  @Value("\${app.publicUrl}")
  lateinit var publicUrl: String

  @Value("\${NODE_ID}")
  lateinit var nodeId: String

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

    trySync()
  }

  private fun trySync() {
    GlobalScope.launch {
      delay(500)
      runCatching {
        if (!isMasterNode()) {
          val request = httpService.prepareGet("${masterUrl}/api/sync")
            .addHeader("x-node-id", nodeId)
            .addHeader("x-version", version)
            .addHeader("x-url", publicUrl)
            .execute()
          val response = request.get()
          log.info(response.responseBody)
        }
      }
    }
  }

  private fun isMasterNode(): Boolean = masterUrl === publicUrl

}
