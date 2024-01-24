package org.migor.feedless.config

import org.ehcache.event.CacheEvent
import org.ehcache.event.CacheEventListener
import org.slf4j.LoggerFactory

class CacheEventLogger : CacheEventListener<Any, Any> {

  private val log = LoggerFactory.getLogger(CacheEventLogger::class.simpleName)

  override fun onEvent(event: CacheEvent<out Any, out Any>?) {
    log.debug("${event?.key} ${event?.type}")
  }
}
