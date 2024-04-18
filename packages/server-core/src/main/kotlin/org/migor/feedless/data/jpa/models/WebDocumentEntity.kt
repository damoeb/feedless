package org.migor.feedless.data.jpa.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.apache.tika.Tika
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.generated.types.Enclosure
import org.migor.feedless.generated.types.WebDocument
import org.migor.feedless.pipeline.PipelineJobEntity
import org.springframework.context.annotation.Lazy
import java.util.*

@Entity
@Table(
  name = "t_web_document", indexes = [
    Index(name = "idx_web_document_url", columnList = "url")
  ]
)
open class WebDocumentEntity : EntityWithUUID() {

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

  @Lazy
  @Column(columnDefinition = "bytea", name = "content_raw") // bytea
  open var contentRaw: ByteArray? = null

  @Column(columnDefinition = "TEXT", name = "content_text")
  open var contentText: String? = null

  @Column(columnDefinition = "TEXT", name = "content_html")
  open var contentHtml: String? = null

  @Column(length = LEN_URL, name = "image_url")
  open var imageUrl: String? = null

  @Column(nullable = false, name = "updated_at")
  open lateinit var updatedAt: Date

  @Column(nullable = false, name = StandardJpaFields.releasedAt)
  open lateinit var releasedAt: Date

  @Column(name = "starting_at")
  open var startingAt: Date? = null

  @Column(nullable = false, name = "score")
  open var score: Int = 0

  @Column(name = "scored_at")
  open var scoredAt: Date? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "webDocumentId")
  open var plugins: MutableList<PipelineJobEntity> = mutableListOf()

  @Column(name = "subscriptionId", nullable = false)
  open lateinit var subscriptionId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = "subscriptionId",
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_item__subscritpion")
  )
  open var subscription: SourceSubscriptionEntity? = null

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "webDocumentId") // todo should be lazy
  open var attachments: MutableList<AttachmentEntity> = mutableListOf()

  @Column(nullable = false, name = StandardJpaFields.status, length = 50)
  @Enumerated(EnumType.STRING)
  open lateinit var status: ReleaseStatus

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

fun WebDocumentEntity.toDto(): WebDocument =
  WebDocument.newBuilder()
    .id(id.toString())
    .imageUrl(imageUrl)
    .url(url)
    .contentTitle(contentTitle)
    .contentText(contentText)
    .contentRawBase64(contentRaw?.let { Base64.getEncoder().encodeToString(contentRaw) })
    .contentRawMime(contentRawMime)
    .updatedAt(updatedAt.time)
    .createdAt(createdAt.time)
    .enclosures(attachments.map {
      Enclosure.newBuilder()
        .url(it.url)
        .type(it.type)
//        .duration(it.duration)
//          .size(it.duration)
        .build()
    })
    .publishedAt(releasedAt.time)
    .startingAt(startingAt?.time)
    .build()
