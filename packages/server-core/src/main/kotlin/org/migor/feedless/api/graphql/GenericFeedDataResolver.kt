package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsData
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.generated.types.GenericFeed
import org.migor.feedless.generated.types.Selectors
import org.migor.feedless.web.WebToFeedTransformer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigInteger
import java.security.MessageDigest

@DgsComponent
class GenericFeedDataResolver {

  @Autowired
  lateinit var webToFeedTransformer: WebToFeedTransformer

//  @DgsData(parentType = DgsConstants.GENERICFEED.TYPE_NAME)
//  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
//  suspend fun nativeFeed(dfe: DgsDataFetchingEnvironment): NativeFeed? = coroutineScope {
//    val feed: GenericFeed = dfe.getSource()
//    feedService.findNativeById(UUID.fromString(feed.id))
//      .map { toDTO(it) }
//      .orElseThrow { IllegalArgumentException("nativeFeed not found") }
//  }

  @DgsData(parentType = DgsConstants.GENERICFEED.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun feedUrl(dfe: DgsDataFetchingEnvironment): String = coroutineScope {
    val feed: GenericFeed = dfe.getSource()
    webToFeedTransformer.createFeedUrl(feed)
  }

  @DgsData(parentType = DgsConstants.GENERICFEED.TYPE_NAME)
  @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
  suspend fun hash(dfe: DgsDataFetchingEnvironment): String = coroutineScope {
    val feed: GenericFeed = dfe.getSource()
    toHash(feed.specification.selectors)
  }

  private fun toHash(selectors: Selectors): String {
    val sha1 = MessageDigest.getInstance("SHA1")
    val input = selectors.linkXPath + selectors.dateXPath + selectors.contextXPath + selectors.extendContext
    return BigInteger(1, sha1.digest(input.toByteArray())).toString(16).padStart(32, '0')
  }


}
