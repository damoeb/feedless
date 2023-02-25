package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.repositories.ArticleDAO
import org.migor.rich.rss.data.jpa.repositories.ContentDAO
import org.migor.rich.rss.generated.types.ArticlesPagedInput
import org.migor.rich.rss.graphql.DtoResolver.fromDto
import org.migor.rich.rss.harvest.feedparser.json.JsonAttachment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Profile(AppProfiles.database)
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

  fun findAllFiltered(data: ArticlesPagedInput): Page<ArticleEntity> {
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
      .map { content ->
        run {
          val richArticle = RichArticle()
          richArticle.id = content.id.toString()
          richArticle.title = content.title!!
          richArticle.url = content.url
//          tags = getTags(content),
          richArticle.attachments = content.attachments.map {
            run {
              val a = JsonAttachment()
              a.url = it.url
              a.type = it.mimeType!!
              a.length = it.length!!
              a
            }
          }
          richArticle.contentText = Optional.ofNullable(StringUtils.trimToNull(content.contentText))
            .orElse(StringUtils.trimToEmpty(content.description))
          richArticle.contentRaw = contentToString(content)
          richArticle.contentRawMime = content.contentRawMime
          richArticle.publishedAt = content.publishedAt
          richArticle.startingAt = content.startingAt
          richArticle.imageUrl = content.imageUrl
          richArticle
        }
      }
  }

//  private fun getTags(content: ContentEntity): List<String>? {
//    val tags = mutableListOf<String>()
//    if (content.hasFulltext) {
//      tags.add("fulltext")
//    }
//    if (content.hasAudio) {
//      tags.add("audio")
//    }
//    if (content.hasVideo) {
//      tags.add("video")
//    }
//    content.contentText?.let {
//      if (it.length <= 140) {
//        tags.add("short")
//      }
//    }
//    return emptyToNull(tags.distinct())
//  }

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

  fun countByStreamId(streamId: UUID): Long {
    return articleDAO.countAllByStreamId(streamId)
  }

}
