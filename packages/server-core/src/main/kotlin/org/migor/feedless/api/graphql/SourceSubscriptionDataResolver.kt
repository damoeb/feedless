package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.data.jpa.repositories.ScrapeSourceDAO
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.generated.types.SourceSubscription
import org.migor.feedless.harvest.toScrapeRequest
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

  @DgsData(parentType = DgsConstants.SOURCESUBSCRIPTION.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun sources(dfe: DgsDataFetchingEnvironment): List<ScrapeRequest> = coroutineScope {
    val source: SourceSubscription = dfe.getSource()
    scrapeRequestDAO.findAllBySubscriptionId(UUID.fromString(source.id)).map { scrapeSource ->
      run {
        val scrapeRequest = scrapeSource.toScrapeRequest()
        scrapeRequest.id = null
        scrapeRequest.corrId = null
        scrapeRequest
      }
    }
  }

}
