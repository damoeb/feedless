package org.migor.rich.rss.graphql

import graphql.kickstart.tools.GraphQLQueryResolver
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.math.NumberUtils
import org.migor.rich.rss.api.dto.FeedDiscovery
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.BucketDAO
import org.migor.rich.rss.discovery.FeedDiscoveryService
import org.migor.rich.rss.generated.ArticleGql
import org.migor.rich.rss.generated.ArticleTypeGql
import org.migor.rich.rss.generated.ArticlesInStreamFilter
import org.migor.rich.rss.generated.DiscoverFeedsInput
import org.migor.rich.rss.generated.EnclosureGql
import org.migor.rich.rss.generated.FeedDiscoveryGql
import org.migor.rich.rss.generated.GenericFeedRuleGql
import org.migor.rich.rss.generated.NativeFeedGql
import org.migor.rich.rss.generated.NativeFeedReferenceGql
import org.migor.rich.rss.generated.ReleaseStatusGql
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.util.CryptUtil.handleCorrId
import org.migor.rich.rss.util.CryptUtil.newCorrId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import java.util.*

@Component
class GraphqlQuery : GraphQLQueryResolver {

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var feedDiscovery: FeedDiscoveryService

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

  fun articles(filter: ArticlesInStreamFilter): List<ArticleGql> {
    val streamId = UUID.fromString(filter.id)
    val page = 0
    val type = filter.type
    val status = filter.status
    val result = articleService.findByStreamId(streamId, page, convertDto(type), convertDto(status))

    return result.toList().map { article -> ArticleGql.builder()
      .setId(article.id)
      .setTitle(article.title)
      .setImageUrl(article.imageUrl)
      .setUrl(article.url)
      .setContentText(article.contentText)
      .setContentRaw(article.contentRaw)
      .setContentRawMime(article.contentRawMime)
      .setTags(article.tags)
      .setPublishedAt(article.publishedAt.time.toDouble())
      .setEnclosures(article.enclosures?.map { enclosure -> EnclosureGql.builder().setUrl(enclosure.url).setType(enclosure.type).build() })
      .build()}
  }

  private fun convertDto(status: ReleaseStatusGql): ReleaseStatus {
    return when(status) {
      ReleaseStatusGql.released -> ReleaseStatus.released
      ReleaseStatusGql.needs_approval -> ReleaseStatus.needs_approval
    }
  }

  private fun convertDto(type: ArticleTypeGql): ArticleType {
    return when(type) {
      ArticleTypeGql.digest -> ArticleType.digest
      ArticleTypeGql.feed -> ArticleType.feed
      ArticleTypeGql.note -> ArticleType.note
      ArticleTypeGql.ops -> ArticleType.ops
    }
  }
}
