package org.migor.rich.rss.service

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.auth.CurrentUser
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.ArticleType
import org.migor.rich.rss.data.jpa.enums.ReleaseStatus
import org.migor.rich.rss.data.jpa.models.ArticleEntity
import org.migor.rich.rss.data.jpa.models.BucketEntity
import org.migor.rich.rss.data.jpa.models.ContentEntity
import org.migor.rich.rss.data.jpa.models.StreamEntity
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
import java.sql.Timestamp
import java.util.*

@Service
@Profile(AppProfiles.database)
class ArticleService {

  @Autowired
  lateinit var contentDAO: ContentDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var currentUser: CurrentUser

  @Transactional(readOnly = true)
  fun findAllByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): List<ContentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, StandardJpaFields.releasedAt))
    return contentDAO.findAllByStreamId(streamId, type, status, pageable)
  }

  fun findAllByFilter(where: ArticlesWhereInput, pageable: PageRequest): List<ArticleEntity> {
    val cb = entityManager.criteriaBuilder
    val query = cb.createQuery(ArticleEntity::class.java)
    val root: Root<ArticleEntity> = query.from(ArticleEntity::class.java)

    val predicates = mutableListOf<Predicate>()
//    val types: List<ArticleType> = where.type?.let { it.oneOf.map { fromDTO(it) } } ?: ArticleType.values().toList()
//    cb.`in`(root.get<String>(StandardJpaFields.type)).`in`(types.map { it.name })
//      .also { predicates.add(it) }

//    val status: List<ReleaseStatus> = where.status?.let { it.oneOf.map { fromDTO(it) } } ?: ReleaseStatus.values().toList()
//    cb.`in`(root.get<String>(StandardJpaFields.status)).`in`(status.map { it.name })
//      .also { predicates.add(it) }
    where.stream?.let {
      it.id?.let {
        cb.equal(root.get<UUID>("streamId"), UUID.fromString(it.equals))
          .also { predicates.add(it) }
      }
      it.bucket?.let { bucket ->
        val joinStream = root.join<ArticleEntity, StreamEntity>("stream")
        val joinBucket = joinStream.join<StreamEntity, BucketEntity>("bucket")
        cb.equal(joinBucket.get<Boolean>("archive"), bucket.archive)
//        joinStream.on(cb.equal(root.get<UUID>("streamId"), joinStream.get<UUID>("id")))

//        cb.equal(root.get<UUID>("streamId"), UUID.fromString(it.equals))
//          .also { predicates.add(it) }
      }
    }

    where.createdAt?.let { createdAt ->
      createdAt.gt?.let {
        cb.greaterThan(root.get(StandardJpaFields.createdAt), Timestamp(it))
      }
      createdAt.lt?.let {
        cb.lessThan(root.get(StandardJpaFields.createdAt), Timestamp(it))
      }
    }

    cb.equal(root.get<UUID>(StandardJpaFields.ownerId), currentUser.userId())
      .also { predicates.add(it) }

    query.where(cb.and(*predicates.toTypedArray()))
      .orderBy(cb.desc(root.get<Timestamp>(StandardJpaFields.createdAt)))

    return entityManager.createQuery(query)
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList
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
          richArticle.contentText = StringUtils.trimToNull(content.contentText) ?: StringUtils.trimToEmpty(content.description)
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
