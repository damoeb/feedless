package org.migor.rich.rss.service

import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.NativeFeedStatus
import org.migor.rich.rss.database.models.StreamEntity
import org.migor.rich.rss.database.repositories.NativeFeedDAO
import org.migor.rich.rss.database.repositories.StreamDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL

@Service
class NativeFeedService {
  private val log = LoggerFactory.getLogger(NativeFeedService::class.simpleName)

  @Autowired
  lateinit var streamDAO: StreamDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  fun createNativeFeed(title: String, url: String, websiteUrl: String, harvestSite: Boolean = false, description: String? = ""): NativeFeedEntity {
    val stream = streamDAO.save(StreamEntity())

    val nativeFeed = NativeFeedEntity()
    nativeFeed.title = title
    nativeFeed.feedUrl = url
    nativeFeed.description = description
    nativeFeed.domain = URL(websiteUrl).host
    nativeFeed.websiteUrl = websiteUrl
    nativeFeed.status = NativeFeedStatus.OK
    nativeFeed.stream = stream
    nativeFeed.harvestSite = harvestSite

    return nativeFeedDAO.save(nativeFeed)
  }

}
