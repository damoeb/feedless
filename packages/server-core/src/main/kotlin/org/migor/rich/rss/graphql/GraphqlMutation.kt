package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLMutationResolver
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.database.enums.BucketVisibility
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.BucketCreateInput
import org.migor.rich.rss.generated.BucketGql
import org.migor.rich.rss.generated.BucketVisibilityGql
import org.migor.rich.rss.generated.LoginResponse
import org.migor.rich.rss.generated.NativeFeedCreateInput
import org.migor.rich.rss.generated.NativeFeedGql
import org.migor.rich.rss.generated.SubscribeInput
import org.migor.rich.rss.generated.SubscriptionGql
import org.migor.rich.rss.generated.UserGql
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NativeFeedService
import org.migor.rich.rss.service.SubscriptionService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException
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
  lateinit var userService: UserService

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  fun login(email: String): LoginResponse {
    val user = userService.getSystemUser()

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

  fun subscribe(data: SubscribeInput): SubscriptionGql {
    val user = userService.getSystemUser()
    // todo mag use data.digest
    val streamId = if (data.where.bucket != null) {
      subscriptionService.subscribeToBucket(UUID.fromString(data.where.bucket.id), false, data.notify, data.filter, user)
        .bucket!!.streamId
    } else {
      subscriptionService.subscribeToFeed(UUID.fromString(data.where.feed.id), false, data.notify, data.filter, user)
        .feed!!.streamId
    }

    return SubscriptionGql.builder()
      .setStreamId(streamId.toString())
      .build()
  }

  fun createBucket(data: BucketCreateInput): BucketGql {
    val corrId = newCorrId()
    val user = userService.getSystemUser()
    val bucket = bucketService.createBucket(corrId,
      name = data.name,
      description = data.description,
      filter = data.filter,
      visibility = toVisibility(data.visibility),
      user = user)

//    data.subscriptions.map { subscription -> subscription. }

    return BucketGql.builder()
      .setId(bucket.id.toString())
      .setName(bucket.name)
      .setDescription(bucket.description)
      .setFilter(bucket.filter)
      .setVisibility(toVisibilityGql(bucket.visibility))
//      .setLastUpdatedAt(bucket.lastUpdatedAt?.toString())
      .build()
  }

  private fun toVisibility(visibility: BucketVisibilityGql): BucketVisibility {
    return when(visibility) {
      BucketVisibilityGql.isPublic -> BucketVisibility.public
      BucketVisibilityGql.isHidden -> BucketVisibility.hidden
      else -> throw IllegalArgumentException("visibility $visibility not supported")
    }
  }

  private fun toVisibilityGql(visibility: BucketVisibility): BucketVisibilityGql {
    return when(visibility) {
      BucketVisibility.public -> BucketVisibilityGql.isPublic
      BucketVisibility.hidden -> BucketVisibilityGql.isHidden
      else -> throw IllegalArgumentException("visibility $visibility not supported")
    }
  }
}
