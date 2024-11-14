package org.migor.feedless.document

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.validation.constraints.Size
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.locationtech.jts.geom.Point
import org.migor.feedless.annotation.AnnotationEntity
import org.migor.feedless.api.isHtml
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.generated.types.Attachment
import org.migor.feedless.generated.types.GeoPoint
import org.migor.feedless.generated.types.Record
import org.migor.feedless.pipeline.DocumentPipelineJobEntity
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.addListenableTag
import org.migor.feedless.repository.classifyDuration
import org.migor.feedless.source.SourceEntity
import org.migor.feedless.util.toMillis
import org.springframework.context.annotation.Lazy
import java.nio.charset.StandardCharsets
import java.sql.Types
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
  name = "t_document",
  indexes = [
    Index(name = "url__idx", columnList = StandardJpaFields.url),
    Index(name = "repository_id__idx", columnList = StandardJpaFields.repositoryId),
  ]
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "type",
  discriminatorType = DiscriminatorType.STRING
)
open class DocumentEntity : EntityWithUUID() {

  companion object {
    const val LEN_50: Int = 50
    const val LEN_STR_DEFAULT: Int = 255
    const val LEN_URL: Int = 1500
  }

  @Size(message = "url", max = LEN_URL)
  @Column(nullable = false, length = LEN_URL, name = StandardJpaFields.url)
  open lateinit var url: String

  @Size(message = "title", max = LEN_STR_DEFAULT)
  @Column(length = LEN_STR_DEFAULT, name = "content_title")
  open var title: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_STR_DEFAULT)
    }

  @Column(length = 50, name = "content_hash", nullable = false)
  open lateinit var contentHash: String

  @Size(message = "rawMimeType", max = LEN_50)
  @Column(length = LEN_50, name = "content_raw_mime")
  open var rawMimeType: String? = null

  @Column(nullable = true, name = "lat_lon", columnDefinition = "geometry")
  open var latLon: Point? = null

  @JdbcTypeCode(Types.ARRAY)
  @Column(name = "tags", columnDefinition = "text[]")
  open var tags: Array<String>? = emptyArray()

  @Lazy
  @Column(columnDefinition = "bytea", name = "content_raw")
  open var raw: ByteArray? = null

  @JdbcTypeCode(Types.LONGVARCHAR)
  @Column(name = "content_text", nullable = false)
  open lateinit var text: String

  @JdbcTypeCode(Types.LONGVARCHAR)
  @Column(name = "content_html")
  open var html: String? = null

  @Size(message = "imageUrl", max = LEN_URL)
  @Column(length = LEN_URL, name = "image_url")
  open var imageUrl: String? = null

  @Column(nullable = false, name = "updated_at")
  open var updatedAt: LocalDateTime = LocalDateTime.now()

  @Column(nullable = false, name = StandardJpaFields.publishedAt)
  open var publishedAt: LocalDateTime = LocalDateTime.now()

  @Column(name = "starting_at")
  open var startingAt: LocalDateTime? = null

  @Column(nullable = false, name = "is_dead")
  open var isDead: Boolean = false

  @Column(nullable = false, name = "is_flagged")
  open var isFlagged: Boolean = false

//  @Column(nullable = false, name = "is_controversial")
//  open var isControversial: Boolean = false

  @Column(nullable = false, name = "score")
  open var score: Int = 0

  @Column(name = "scored_at")
  open var scoredAt: LocalDateTime? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "documentId")
  open var plugins: MutableList<DocumentPipelineJobEntity> = mutableListOf()

  @Column(name = StandardJpaFields.repositoryId, nullable = false)
  open lateinit var repositoryId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.repositoryId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_document__to__repository")
  )
  open var repository: RepositoryEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var votes: MutableList<AnnotationEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "documentId", cascade = [CascadeType.ALL])
  open var attachments: MutableList<AttachmentEntity> = mutableListOf()

  @Column(nullable = false, name = StandardJpaFields.status, length = 50)
  @Enumerated(EnumType.STRING)
  open lateinit var status: ReleaseStatus

  @Column(name = "parent_id", nullable = true)
  open var parentId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "parent_id",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_document__to__parent")
  )
  open var parent: DocumentEntity? = null

  @Column(name = StandardJpaFields.sourceId)
  open var sourceId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.sourceId,
    referencedColumnName = StandardJpaFields.id,
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_document__to__source")
  )
  open var source: SourceEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var children: MutableList<DocumentEntity> = mutableListOf()

//  @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
//  @JoinTable(
//    name = "content_to_tag",
//    joinColumns = [
//      JoinColumn(
//        name = "content_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )],
//    inverseJoinColumns = [
//      JoinColumn(
//        name = "tag_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )
//    ]
//  )
//  open var tags: List<TagEntity> = mutableListOf()

  @PrePersist
  fun prePersist() {
    if (raw != null) {
      val tika = Tika()
      val mime = tika.detect(raw)
//      log.info("record $id with raw-data '$mime'")
      this.rawMimeType = mime
    }
  }

}

fun DocumentEntity.toDto(propertyService: PropertyService): Record {
  val htmlParam: String?
  var rawBase64Param: String? = null
  var rawMimeTypeParam: String? = null
  if (StringUtils.isBlank(html) && isHtml(rawMimeType)) {
    htmlParam = raw?.toString(StandardCharsets.UTF_8)
  } else {
    htmlParam = html
    rawBase64Param = raw?.let { Base64.getEncoder().encodeToString(raw) }
    rawMimeTypeParam = rawMimeType
  }

  return Record(
    id = id.toString(),
    imageUrl = imageUrl,
    url = url,
    html = htmlParam,
    rawBase64 = rawBase64Param,
    rawMimeType = rawMimeTypeParam,
    title = title,
    text = text,
    createdAt = createdAt.toMillis(),
    updatedAt = updatedAt.toMillis(),
    latLng = latLon?.let {
      GeoPoint(
        lat = it.x,
        lon = it.y,
      )
    },
    tags = (tags?.asList() ?: emptyList()).plus(
      addListenableTag(attachments.filter { it.mimeType.startsWith("audio/") && it.duration != null }
        .map { classifyDuration(it.duration!!) }.distinct()
      )
    ),

    attachments = (attachments.map {
      Attachment(
        url = it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id),
        type = it.mimeType,
        duration = it.duration,
        size = it.size,
      )
    }),
    publishedAt = publishedAt.toMillis(),
    startingAt = startingAt?.toMillis(),
  )
}

fun createDocumentUrl(propertyService: PropertyService, id: UUID): String =
  "${propertyService.apiGatewayUrl}/article/${id}"
