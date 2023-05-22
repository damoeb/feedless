package org.migor.feedless.service

import jakarta.persistence.EntityManager
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.graphql.DtoResolver.fromDTO
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ArticleType
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.data.jpa.models.ArticleEntity
import org.migor.feedless.data.jpa.models.BucketEntity
import org.migor.feedless.data.jpa.models.StreamEntity
import org.migor.feedless.data.jpa.models.WebDocumentEntity
import org.migor.feedless.data.jpa.repositories.ArticleDAO
import org.migor.feedless.data.jpa.repositories.BucketDAO
import org.migor.feedless.data.jpa.repositories.WebDocumentDAO
import org.migor.feedless.feed.parser.json.JsonAttachment
import org.migor.feedless.generated.types.ArticleInput
import org.migor.feedless.generated.types.ArticleMultipleWhereInput
import org.migor.feedless.generated.types.ArticlesWhereInput
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
  lateinit var webDocumentDAO: WebDocumentDAO

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var articleDAO: ArticleDAO

  @Autowired
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var currentUser: CurrentUser

  @Transactional(readOnly = true)
  fun findAllByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): List<WebDocumentEntity> {
    val pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, StandardJpaFields.releasedAt))
    return webDocumentDAO.findAllByStreamId(streamId, type, status, pageable)
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
        bucket.tags?.let {
          val userId = currentUser.userId()!!
          val offset = 0
          val limit = 100 // todo mag create a table
          val bucketIds = (it.some?.let { bucketDAO.findAllByOwnerIdAndSomeTags(userId, it.toTypedArray(), offset, limit) }
            ?: bucketDAO.findAllByOwnerIdAndEveryTags(userId, it.every.toTypedArray(), offset, limit))
            .map { it.id }
          val joinStream = root.join<ArticleEntity, StreamEntity>("stream")
          val joinBucket = joinStream.join<StreamEntity, BucketEntity>("bucket")
          predicates.add(joinBucket.get<UUID>("id").`in`(bucketIds))
        }
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

    val joinWebDocument = root.join<ArticleEntity, WebDocumentEntity>("webDocument")
    query.where(cb.and(*predicates.toTypedArray()))
      .orderBy(cb.desc(joinWebDocument.get<Timestamp>(StandardJpaFields.releasedAt)))

    return entityManager.createQuery(query)
      .setFirstResult(pageable.pageNumber * pageable.pageSize)
      .setMaxResults(pageable.pageSize)
      .resultList
  }

  @Transactional(readOnly = true)
  fun findByStreamId(streamId: UUID, page: Int, type: ArticleType, status: ReleaseStatus): List<RichArticle> {
    return findAllByStreamId(streamId, page, type, status)
      .map { webDocument ->
        run {
          val richArticle = RichArticle()
          richArticle.id = webDocument.id.toString()
          richArticle.title = webDocument.title!!
          richArticle.url = webDocument.url
//          tags = getTags(content),
          webDocument.attachments?.let {
            richArticle.attachments = it.media.map {
              run {
                val a = JsonAttachment()
                a.url = it.url
                a.type = it.format!!
//                a.size = it.size
                a.duration = it.duration
                a
              }
            }
          }
          richArticle.contentText = StringUtils.trimToNull(webDocument.contentText) ?: StringUtils.trimToEmpty(webDocument.description)
          richArticle.contentRaw = contentToString(webDocument)
          richArticle.contentRawMime = webDocument.contentRawMime
          richArticle.publishedAt = webDocument.releasedAt
          richArticle.startingAt = webDocument.startingAt
          richArticle.imageUrl = webDocument.imageUrl
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

  private fun contentToString(webDocument: WebDocumentEntity): String? {
    return if (StringUtils.startsWith(webDocument.contentRawMime, "text")) {
      webDocument.contentRaw!!
    } else {
      null
    }
  }

  fun findById(id: UUID): Optional<ArticleEntity> {
    return articleDAO.findById(id)
  }

  fun deleteAllByFilter(where: ArticleMultipleWhereInput) {
    articleDAO.deleteAllByIdIn(where.`in`.map { UUID.fromString(it.id) }, currentUser.userId()!!)
  }

  fun updateAllByFilter(where: ArticleMultipleWhereInput, data: ArticleInput) {
    articleDAO.updateAllByIdIn(where.`in`.map { UUID.fromString(it.id) },
      Optional.ofNullable(data.status).map { fromDTO(it.set) }
        .orElseThrow { IllegalArgumentException("article not found") },
      currentUser.userId()!!
    )
  }

}
