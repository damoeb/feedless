package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.Activity
import org.migor.feedless.generated.types.ActivityItem
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.harvest.toScrapeRequest
import org.migor.feedless.service.FrequencyItem
import org.migor.feedless.service.WebDocumentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class SourceSubscriptionDataResolver {

  @Autowired
  lateinit var scrapeRequestDAO: ScrapeSourceDAO

  @Autowired
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun sources(dfe: DgsDataFetchingEnvironment): List<ScrapeRequest> = coroutineScope {
    val source: SourceSubscription = dfe.getSource()
    scrapeRequestDAO.findAllBySubscriptionId(UUID.fromString(source.id)).map { scrapeSource ->
      run {
        val scrapeRequest = scrapeSource.toScrapeRequest()
        scrapeRequest.corrId = null
        scrapeRequest
      }
    }
  }

  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun documentCount(dfe: DgsDataFetchingEnvironment): Long = coroutineScope {
    val source: SourceSubscription = dfe.getSource()
    webDocumentDAO.countBySubscriptionId(UUID.fromString(source.id))
  }

  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  suspend fun activity(
    dfe: DgsDataFetchingEnvironment,
  ): Activity = coroutineScope {
    val source: SourceSubscription = dfe.getSource()

    Activity.newBuilder()
      .items(webDocumentService.getWebDocumentFrequency(UUID.fromString(source.id)).map { it.toDto() })
      .build()
  }

}

private fun FrequencyItem.toDto(): ActivityItem {
  fun leftPad(num: Int): String {
    return StringUtils.leftPad("$num", 2, "0")
  }

  return ActivityItem.newBuilder()
    .index("${year}${leftPad(month)}${leftPad(day)}")
    .count(count)
    .build()
}
