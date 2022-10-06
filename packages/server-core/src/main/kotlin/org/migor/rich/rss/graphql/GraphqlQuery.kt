package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import org.apache.commons.lang3.BooleanUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.ArticleGql
import org.migor.rich.rss.generated.ArticleTypeGql
import org.migor.rich.rss.generated.ArticlesInStreamFilter
import org.migor.rich.rss.generated.ArticlesInStreamGql
import org.migor.rich.rss.generated.DiscoverFeedsInput
import org.migor.rich.rss.generated.EnclosureGql
import org.migor.rich.rss.generated.FeedDiscoveryGql
import org.migor.rich.rss.generated.GenericFeedRuleGql
import org.migor.rich.rss.generated.NativeFeedReferenceGql
import org.migor.rich.rss.generated.ReleaseStatusGql
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.SubscriptionService
import org.migor.rich.rss.service.UserService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*

@Component
class GraphqlQuery : GraphQLQueryResolver {

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

  @Autowired
  lateinit var userService: UserService

  fun discoverFeeds(data: DiscoverFeedsInput): FeedDiscoveryGql {
    val corrId = handleCorrId(data.corrId)
    val discovery = feedDiscovery.discoverFeeds(corrId, data.url, null, BooleanUtils.isTrue(data.prerender), false)
    val response = discovery.results
    return FeedDiscoveryGql.builder()
      .setFailed(response.failed)
      .setMimeType(response.mimeType)
      .setErrorMessage(response.errorMessage)
      .setGenericFeedRules(response.genericFeedRules.map { GenericFeedRuleGql.builder()
        .setFeedUrl(it.feedUrl)
        .setCount(it.count)
        .setContextXPath(it.contextXPath)
        .setDateXPath(it.dateXPath)
        .setExtendContext(it.extendContext)
        .setLinkXPath(it.linkXPath)
        .setScore(it.score)
        .build()
      })
      .setNativeFeeds(response.nativeFeeds.map { NativeFeedReferenceGql.builder()
        .setUrl(it.url)
        .setTitle(it.title)
        .setType(it.type!!.name)
        .setDescription(it.description)
        .build()
      })
      .build()
  }

  fun articles(filter: ArticlesInStreamFilter): ArticlesInStreamGql {
    val streamId = UUID.fromString(filter.id)
    val page = 0
    val type = filter.type
    val status = filter.status
    val result = articleService.findByStreamId(streamId, page, convertDto(type), convertDto(status))

    return ArticlesInStreamGql.builder()
      .setIsLast(result.isLast)
      .setIsFirst(result.isFirst)
      .setIsEmpty(result.isEmpty)
      .setPage(page)
      .setTotalPages(result.totalPages)
      .setArticles(result.toList().map { article -> toArticle(article) })
      .build()
  }

  private fun toArticle(article: RichArticle): ArticleGql =
    ArticleGql.builder()
      .setId(article.id)
      .setTitle(article.title)
      .setImageUrl(article.imageUrl)
      .setUrl(article.url)
      .setContentText(article.contentText)
      .setContentRaw(article.contentRaw)
      .setContentRawMime(article.contentRawMime)
      .setTags(article.tags)
      .setPublishedAt(article.publishedAt.time.toDouble())
      .setEnclosures(article.enclosures?.map { enclosure ->
        EnclosureGql.builder().setUrl(enclosure.url).setType(enclosure.type).build()
      })
      .build()

  private fun convertDto(status: ReleaseStatusGql): ReleaseStatus {
    return when(status) {
      ReleaseStatusGql.released -> ReleaseStatus.released
      ReleaseStatusGql.needs_approval -> ReleaseStatus.needs_approval
      else -> throw IllegalArgumentException("ReleaseStatus $status not supported")
    }
  }

  private fun convertDto(type: ArticleTypeGql): ArticleType {
    return when(type) {
      ArticleTypeGql.digest -> ArticleType.digest
      ArticleTypeGql.feed -> ArticleType.feed
      else -> throw IllegalArgumentException("ArticleType $type not supported")
    }
  }
}
