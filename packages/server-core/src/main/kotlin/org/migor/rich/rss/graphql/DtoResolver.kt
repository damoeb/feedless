package org.migor.rich.rss.graphql

import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.GenericFeedEntity
import org.migor.rich.rss.database.models.ImporterEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.models.WebDocumentEntity
import org.migor.rich.rss.generated.ArticleDto
import org.migor.rich.rss.generated.ArticleTypeDto
import org.migor.rich.rss.generated.BucketDto
import org.migor.rich.rss.generated.ContentDto
import org.migor.rich.rss.generated.GenericFeedDto
import org.migor.rich.rss.generated.ImporterDto
import org.migor.rich.rss.generated.NativeFeedDto
import org.migor.rich.rss.generated.PagedArticlesResponseDto
import org.migor.rich.rss.generated.PaginationDto
import org.migor.rich.rss.generated.ReleaseStatusDto
import org.migor.rich.rss.generated.UserDto
import org.migor.rich.rss.generated.WebDocumentDto
import org.migor.rich.rss.util.JsonUtil
import org.springframework.data.domain.Page

object DtoResolver {

  fun toDTO(content: ContentEntity): ContentDto? =
    ContentDto.builder()
      .setId(content.id.toString())
      .setTitle(content.title)
      .setImageUrl(content.imageUrl)
      .setUrl(content.url)
      .setDescription(content.description)
      .setContentTitle(content.contentTitle)
      .setContentText(content.contentText)
      .setContentRaw(content.contentRaw)
      .setContentRawMime(content.contentRawMime)
      .setUpdatedAt(content.updatedAt?.time)
      .setCreatedAt(content.createdAt.time)
      .setHasFulltext(content.hasFulltext)
      .setTags(getTags(content))
      .setPublishedAt(content.publishedAt?.time)
      .build()


  private fun getTags(content: ContentEntity): List<String> {
    val tags = mutableListOf<String>()
    if (content.hasFulltext) {
      tags.add("fulltext")
    }
    if (content.hasAudio) {
      tags.add("audio")
    }
    if (content.hasVideo) {
      tags.add("video")
    }
    content.contentText?.let {
      if (it.length <= 140) {
        tags.add("short")
      }
    }
    return tags.distinct()
  }


  fun <T> toPaginatonDTO(page: Page<T>): PaginationDto =
    PaginationDto.builder()
      .setIsEmpty(page.isEmpty)
      .setIsFirst(page.isFirst)
      .setIsLast(page.isLast)
      .setPage(page.number)
      .setTotalElements(page.totalElements)
      .setTotalPages(page.totalPages)
      .build()


  fun toDTO(article: ArticleEntity): ArticleDto =
    ArticleDto.builder()
      .setId(article.id.toString())
      .setContentId(article.contentId.toString())
      .setStreamId(article.streamId.toString())
      .setNativeFeedId(article.feedId.toString())
//      .setContent(toArticleContent(article.content!!))
      .setType(toDTO(article.type))
      .setStatus(toDTO(article.status))
      .setCreatedAt(article.createdAt.time)
      .build()

  fun toDTO(d: WebDocumentEntity): WebDocumentDto =
    WebDocumentDto.builder()
      .setId(d.id.toString())
      .setType(d.type)
      .setUrl(d.url)
      .setTitle(d.title)
      .setDescription(d.description)
      .setScore(d.score)
      .setImageUrl(d.imageUrl)
      .setCreatedAt(d.createdAt.time)
      .build()

  fun toDTO(result: Page<ArticleEntity>): PagedArticlesResponseDto =
    PagedArticlesResponseDto.builder()
      .setPagination(toPaginatonDTO(result))
      .setArticles(result.toList().map { toDTO(it) })
      .build()

  fun toDTO(status: ReleaseStatus): ReleaseStatusDto = when (status) {
    ReleaseStatus.released -> ReleaseStatusDto.released
    ReleaseStatus.needs_approval -> ReleaseStatusDto.needs_approval
    else -> throw IllegalArgumentException("ReleaseStatus $status not supported")
  }

  fun toDTO(type: ArticleType): ArticleTypeDto = when (type) {
    ArticleType.digest -> ArticleTypeDto.digest
    ArticleType.feed -> ArticleTypeDto.feed
    else -> throw IllegalArgumentException("ArticleType $type not supported")
  }

  fun fromDto(status: ReleaseStatusDto) = when (status) {
    ReleaseStatusDto.released -> ReleaseStatus.released
    ReleaseStatusDto.needs_approval -> ReleaseStatus.needs_approval
    else -> throw IllegalArgumentException("ReleaseStatus $status not supported")
  }

  fun fromDto(type: ArticleTypeDto): ArticleType = when (type) {
    ArticleTypeDto.digest -> ArticleType.digest
    ArticleTypeDto.feed -> ArticleType.feed
    else -> throw IllegalArgumentException("ArticleType $type not supported")
  }

  fun toDTO(it: ImporterEntity): ImporterDto = ImporterDto.builder()
    .setId(it.id.toString())
    .setAutoRelease(it.autoRelease)
    .setCreatedAt(it.createdAt.time)
    .setNativeFeedId(it.feedId.toString())
    .setBucketId(it.bucketId.toString())
    .build()


  fun toDTO(bucket: BucketEntity): BucketDto = BucketDto.builder()
    .setTitle(bucket.name)
    .setDescription(bucket.description)
    .setId(bucket.id.toString())
    .setWebsiteUrl(bucket.websiteUrl)
    .setImageUrl(bucket.imageUrl)
    .setStreamId(bucket.streamId.toString())
    .setCreatedAt(bucket.createdAt.time)
    .build()


  fun toDTO(it: GenericFeedEntity?): GenericFeedDto? {
    return if (it == null) {
      null
    } else {
      GenericFeedDto.builder()
        .setId(it.id.toString())
        .setNativeFeedId(it.managingFeedId.toString())
        .setFeedRule(JsonUtil.gson.toJson(it.feedRule))
        .setCreatedAt(it.createdAt.time)
        .build()
    }
  }

  fun toDTO(it: NativeFeedEntity): NativeFeedDto =
    NativeFeedDto.builder()
      .setId(it.id.toString())
      .setTitle(it.title)
      .setDescription(it.description)
      .setImageUrl(it.imageUrl)
      .setWebsiteUrl(it.websiteUrl)
      .setFeedUrl(it.feedUrl)
      .setDomain(it.domain)
      .setStreamId(it.streamId.toString())
      .setGenericFeed(toDTO(it.managedBy))
      .setStatus(it.status.toString())
      .setLastUpdatedAt(it.lastUpdatedAt?.time)
      .setCreatedAt(it.createdAt.time)
      .build()


  fun toDTO(user: UserEntity): UserDto =
    UserDto.builder()
      .setId(user.id.toString())
      .setDateFormat(user.dateFormat)
      .setTimeFormat(user.timeFormat)
      .setEmail(user.email)
      .setName(user.name)
      .setCreatedAt(user.createdAt.time)
      .build()

}
