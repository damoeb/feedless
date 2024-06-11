package org.migor.feedless.document

import io.hypersistence.utils.hibernate.type.array.StringArrayType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.hibernate.annotations.Type
import org.migor.feedless.annotation.AnnotationEntity
import org.migor.feedless.api.isHtml
import org.migor.feedless.attachment.AttachmentEntity
import org.migor.feedless.attachment.createAttachmentUrl
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.generated.types.Enclosure
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.pipeline.PipelineJobEntity
import org.migor.feedless.repository.RepositoryEntity
import org.migor.feedless.repository.addListenableTag
import org.migor.feedless.repository.classifyDuration
import org.springframework.context.annotation.Lazy
import java.nio.charset.StandardCharsets
import java.util.*

@Entity
@Table(
  name = "t_document", indexes = [
//  Index(name = "idx_document_url", columnList = "url, repository_id")
  ]
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
  name = "type",
  discriminatorType = DiscriminatorType.STRING
)
open class DocumentEntity : EntityWithUUID() {

  companion object {
    const val LEN_TITLE = 256
    const val LEN_URL = 1000
  }

  @Column(nullable = false, length = LEN_URL, name = "url")
  open lateinit var url: String

  @Column(length = LEN_TITLE, name = "content_title")
  open var contentTitle: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_TITLE)
    }

  @Column(length = 50, name = "content_raw_mime")
  open var contentRawMime: String? = null

  @Type(StringArrayType::class)
  @Column(name = "tags", columnDefinition = "text[]")
  open var tags: Array<String>? = emptyArray()

  @Lazy
  @Column(columnDefinition = "bytea", name = "content_raw")
  open var contentRaw: ByteArray? = null

  @Column(columnDefinition = "TEXT", name = "content_text", nullable = false)
  open lateinit var contentText: String

  @Column(columnDefinition = "TEXT", name = "content_html")
  open var contentHtml: String? = null

  @Column(length = LEN_URL, name = "image_url")
  open var imageUrl: String? = null

  @Column(nullable = false, name = "updated_at")
  open lateinit var updatedAt: Date

  @Column(nullable = false, name = StandardJpaFields.publishedAt)
  open lateinit var publishedAt: Date

  @Column(name = "starting_at")
  open var startingAt: Date? = null

  @Column(nullable = false, name = "is_dead")
  open var isDead: Boolean = false

  @Column(nullable = false, name = "is_flagged")
  open var isFlagged: Boolean = false

//  @Column(nullable = false, name = "is_controversial")
//  open var isControversial: Boolean = false

  @Column(nullable = false, name = "score")
  open var score: Int = 0

  @Column(name = "scored_at")
  open var scoredAt: Date? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "documentId")
  open var plugins: MutableList<PipelineJobEntity> = mutableListOf()

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

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "documentId", cascade = [CascadeType.ALL])
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
    if (contentRaw != null) {
      val tika = Tika()
      val mime = tika.detect(contentRaw)
//      log.info("webDocument $id with raw-data '$mime'")
      this.contentRawMime = mime
    }
  }

}

fun DocumentEntity.toDto(propertyService: PropertyService): WebDocument {
  val builder = WebDocument.newBuilder()
    .id(id.toString())
    .imageUrl(imageUrl)
    .url(url)
    .contentTitle(contentTitle)
    .contentText(contentText)
    .updatedAt(updatedAt.time)
    .createdAt(createdAt.time)
    .tags((tags?.asList() ?: emptyList()).plus(
      addListenableTag(attachments.filter { it.contentType.startsWith("audio/") && it.duration != null }
        .map { classifyDuration(it.duration!!) }.distinct()
      )
    )
    )
    .enclosures(attachments.map {
      Enclosure.newBuilder()
        .url(it.remoteDataUrl ?: createAttachmentUrl(propertyService, it.id))
        .type(it.contentType)
        .duration(it.duration)
        .size(it.size)
        .build()
    })
    .publishedAt(publishedAt.time)
    .startingAt(startingAt?.time)

  return if (StringUtils.isBlank(contentHtml) && isHtml(contentRawMime)) {
    builder.contentHtml(contentRaw?.toString(StandardCharsets.UTF_8))
      .build()
  } else {
    builder
      .contentHtml(contentHtml)
      .contentRawBase64(contentRaw?.let { Base64.getEncoder().encodeToString(contentRaw) })
      .contentRawMime(contentRawMime)
      .build()
  }
}

fun createDocumentUrl(propertyService: PropertyService, id: UUID): String = "${propertyService.apiGatewayUrl}/article/${id}"
