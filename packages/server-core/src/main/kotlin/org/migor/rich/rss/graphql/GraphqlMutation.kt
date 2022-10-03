package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLMutationResolver
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.database.enums.BucketVisibility
import org.migor.rich.rss.database.repositories.UserDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.BucketCreateInput
import org.migor.rich.rss.generated.BucketGql
import org.migor.rich.rss.generated.LoginResponse
import org.migor.rich.rss.generated.NativeFeedCreateInput
import org.migor.rich.rss.generated.NativeFeedGql
import org.migor.rich.rss.generated.UserGql
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class GraphqlMutation : GraphQLMutationResolver {

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var feedDiscoveryService: FeedDiscoveryService

  @Autowired
  lateinit var nativeFeedService: NativeFeedService

  @Autowired
  lateinit var bucketService: BucketService

  @Autowired
  lateinit var userDAO: UserDAO

  fun login(email: String): LoginResponse {
    val user = Optional.ofNullable(userDAO.findByName("system")).orElseThrow()

    return LoginResponse.builder()
      .setToken(authService.createTokenForUser(user))
      .setUser(UserGql.builder()
        .setId(user.id.toString())
        .setDateFormat(user.dateFormat)
        .setTimeFormat(user.timeFormat)
        .setEmail(user.email)
        .setName(user.name)
        .build()
      )
      .build()
  }

  fun createNativeFeed(data: NativeFeedCreateInput): NativeFeedGql {
    val corrId = newCorrId()
    val feed = feedDiscoveryService.discoverFeeds(corrId, data.feedUrl).results.nativeFeeds.first()
    val nativeFeed = nativeFeedService.createNativeFeed(
      Optional.ofNullable(data.title).orElse(feed.title),
      data.feedUrl,
      data.websiteUrl,
      BooleanUtils.isTrue(data.harvestSite),
      feed.description
    )

    return NativeFeedGql.builder()
      .setId(nativeFeed.id.toString())
      .setTitle(nativeFeed.title)
      .setFeedUrl(nativeFeed.feedUrl)
      .setDescription(nativeFeed.description)
      .setDomain(nativeFeed.domain)
      .setWebsiteUrl(nativeFeed.websiteUrl)
      .setStatus(nativeFeed.status.name)
      .build()
  }

  fun createBucket(data: BucketCreateInput): BucketGql {
    val corrId = newCorrId()
    val user = Optional.ofNullable(userDAO.findByName("system")).orElseThrow()
    val visibility = if (data.listed) {
      BucketVisibility.public
    } else {
      BucketVisibility.hidden
    }
    val bucket = bucketService.createBucket(corrId,
      data.name,
      data.name,
      visibility,
      user)

    return BucketGql.builder()
      .setId(bucket.id.toString())
      .setName(bucket.name)
      .setDescription(bucket.description)
      .build()
  }
}
