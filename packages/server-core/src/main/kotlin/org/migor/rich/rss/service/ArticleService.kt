package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.api.dto.RichEnclosure
import org.migor.rich.rss.database.enums.ArticleType
import org.migor.rich.rss.database.enums.ReleaseStatus
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.ContentEntity
import org.migor.rich.rss.database.models.HarvestTaskEntity
import org.migor.rich.rss.database.models.NativeFeedEntity
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.AttachmentDAO
import org.migor.rich.rss.database.repositories.ContentDAO
import org.migor.rich.rss.database.repositories.HarvestTaskDAO
import org.migor.rich.rss.generated.ArticlesPagedInputDto
import org.migor.rich.rss.graphql.DtoResolver.fromDto
import org.migor.rich.rss.service.HarvestTaskService.Companion.isBlacklistedForHarvest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.MimeType
import java.util.*

@Service
@Profile("database")
class ArticleService {

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Transactional(readOnly = true)
  fun findAllByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): Page<ContentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "publishedAt"))
    return contentDAO.findAllByStreamId(streamId, type, status, pageable)
  }

  fun findAllFiltered(data: ArticlesPagedInputDto): Page<ArticleEntity> {
    val streamId = data.where.streamId
    val page = data.page
    val types = if (data.where.type == null) {
      ArticleType.values()
    } else {
      data.where.type.oneOf.map { fromDto(it) }.toTypedArray()
    }
    val status = if (data.where.status == null) {
      ReleaseStatus.values()
    } else {
      data.where.status.oneOf.map { fromDto(it) }.toTypedArray()
    }

    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "releasedAt"))
    return articleDAO.findAllByStreamId(UUID.fromString(streamId), types, status, pageable)
  }

  @Transactional(readOnly = true)
  fun findByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): Page<RichArticle> {
    return findAllByStreamId(streamId, page, type, status)
      .map { article ->
        RichArticle(
          id = article.id.toString(),
          title = article.title!!,
          url = article.url!!,
          tags = getTags(article),
          enclosures = emptyToNull(article.attachments)?.map { a -> RichEnclosure(a.length, a.mimeType!!, a.url!!) },
          contentText = article.contentText!!,
          contentRaw = contentToString(article),
          contentRawMime = article.contentRawMime,
          publishedAt = article.publishedAt!!,
          imageUrl = article.imageUrl
        )
      }
  }

  private fun getTags(content: ContentEntity): List<String>? {
    val tags = mutableListOf<String>()
    if (content.hasFulltext) {
      tags.add("fulltext")
    }
    content.contentText?.let {
      if (it.length <= 140) {
        tags.add("short")
      }
    }
    content.attachments?.let {
      val mainTypes = it.map {
        it.mimeType
      }.plus(content.contentRawMime)
        .filterNotNull()
        .map { MimeType.valueOf(it).type }
        .distinct()

      if (mainTypes.contains("video")) {
        tags.add("video")
      }
      if (mainTypes.contains("audio")) {
        tags.add("audio")
      }
    }
    // todo mag add from url
    return emptyToNull(tags.distinct())
  }

  private fun <T> emptyToNull(list: List<T>?): List<T>? {
    return if (list.isNullOrEmpty()) {
      null
    } else {
      list
    }
  }

  private fun contentToString(content: ContentEntity): String? {
    return if (StringUtils.startsWith(content.contentRawMime, "text")) {
      content.contentRaw!!
    } else {
      null
    }
  }

  fun findById(id: UUID): Optional<ArticleEntity> {
    return articleDAO.findById(id)
  }

  fun findContentById(id: UUID): Optional<ContentEntity> {
    return contentDAO.findById(id)
  }
}
