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
import org.migor.rich.rss.generated.types.ArticleInput
import org.migor.rich.rss.generated.types.ArticleMultipleWhereInput
import org.migor.rich.rss.generated.types.ArticlesWhereInput
import org.migor.rich.rss.graphql.DtoResolver.fromDTO
import org.migor.rich.rss.harvest.feedparser.json.JsonAttachment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
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
  fun findAllByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): List<ContentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "publishedAt"))
    return contentDAO.findAllByStreamId(streamId, type, status, pageable)
  }

  fun findAllByFilter(where: ArticlesWhereInput, pageable: PageRequest): List<ArticleEntity> {
    val streamId = where.streamId
    val types = if (where.type == null) {
      ArticleType.values()
    } else {
      where.type.oneOf.map { fromDTO(it) }.toTypedArray()
    }
    val status = if (where.status == null) {
      ReleaseStatus.values()
    } else {
      where.status.oneOf.map { fromDTO(it) }.toTypedArray()
    }

    return articleDAO.findAllByStreamId(UUID.fromString(streamId), types, status, pageable)
  }

  @Transactional(readOnly = true)
  fun findByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): List<RichArticle> {
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
              a.size = it.size
              a.duration = it.duration
              a
            }
          }
          richArticle.contentText = Optional.ofNullable(StringUtils.trimToNull(content.contentText))
            .orElse(StringUtils.trimToEmpty(content.description))
          richArticle.contentRaw = contentToString(content)
          richArticle.contentRawMime = content.contentRawMime
          richArticle.publishedAt = content.releasedAt
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

  fun deleteAllByFilter(where: ArticleMultipleWhereInput) {
    articleDAO.deleteAllByIdIn(where.`in`.map { UUID.fromString(it.id) })
  }

  fun updateAllByFilter(where: ArticleMultipleWhereInput, data: ArticleInput) {
    articleDAO.updateAllByIdIn(where.`in`.map { UUID.fromString(it.id) },
      Optional.ofNullable(data.status).map { fromDTO(it.set) }.orElseThrow()
    )
  }

}
