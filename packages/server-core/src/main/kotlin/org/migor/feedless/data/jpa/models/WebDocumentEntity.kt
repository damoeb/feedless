package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
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
import org.hibernate.annotations.Type
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.ReleaseStatus
import org.migor.feedless.generated.types.Enclosure
import org.migor.feedless.generated.types.WebDocument
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

  @Basic
  @Column(nullable = false, length = LEN_URL)
  open lateinit var url: String

  @Basic
  @Column(length = LEN_TITLE)
  open var contentTitle: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_TITLE)
    }

  @Basic
  @Column(length = 50)
  open var contentRawMime: String? = null

  @Column(columnDefinition = "bytea") // bytea
  @Basic(fetch = FetchType.LAZY)
  open var contentRaw: ByteArray? = null

  @Column(columnDefinition = "TEXT")
  open var contentText: String? = null

  @Column(columnDefinition = "TEXT")
  open var contentHtml: String? = null

  @Basic
  @Column(length = LEN_URL)
  open var imageUrl: String? = null

  @Basic
  @Column(nullable = false)
  open lateinit var updatedAt: Date

  @Basic
  @Column(nullable = false, name = StandardJpaFields.releasedAt)
  open lateinit var releasedAt: Date

  @Basic
  open var startingAt: Date? = null

  @Basic
  @Column(nullable = false)
  open var score: Int = 0

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false, name = "pending_plugins")
  @Basic(fetch = FetchType.LAZY)
  open var pendingPlugins: List<String> = emptyList()

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false, name = "executed_plugins")
  @Basic(fetch = FetchType.LAZY)
  open var executedPlugins: List<String> = emptyList()

  @Basic
  open var pluginsCoolDownUntil: Date? = null

  @Basic
  @Column(nullable = false)
  open var finalized: Boolean = false

  @Basic
  @Column(name = "subscriptionId", nullable = false)
  open lateinit var subscriptionId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(name = "subscriptionId", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = ForeignKey(name = "fk_item__subscritpion"))
  open var subscription: SourceSubscriptionEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "webDocumentId")
  open var attachments: MutableList<AttachmentEntity> = mutableListOf()

  @Basic
  @Column(nullable = false, name = StandardJpaFields.status, length = 50)
  @Enumerated(EnumType.STRING)
  open var status: ReleaseStatus = ReleaseStatus.released

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
      mime.toString()
    }
    finalized = pendingPlugins.isEmpty()
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
    .pendingPlugins(pendingPlugins)
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
