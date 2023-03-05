package org.migor.rich.rss.service

import io.micrometer.core.annotation.Timed
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.transform.GenericFeedFetchOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.grpc)
class GrpcPuppeteerService: PuppeteerService {

  private val log = LoggerFactory.getLogger(PuppeteerService::class.simpleName)

  @Autowired
  lateinit var feedService: FeedService

  @Autowired
  lateinit var httpService: HttpService

  @Value("\${app.puppeteerHost:#{null}}")
  lateinit var puppeteerHost: Optional<String>

  override fun canPrerender(): Boolean = false
  override fun hasHost(): Boolean = puppeteerHost.map { StringUtils.isNoneBlank(it) }.orElse(false)

  @Timed
  override fun prerender(
    corrId: String,
    options: GenericFeedFetchOptions,
  ): PuppeteerHttpResponse {
    TODO()
  }
}
