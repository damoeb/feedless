package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.apache.commons.lang3.StringUtils
import org.migor.rich.rss.data.jpa.EntityWithUUID
import org.migor.rich.rss.data.jpa.StandardJpaFields
import org.migor.rich.rss.data.jpa.enums.ArticleSource
import java.util.*

@Entity
@Table(
  name = "t_content", indexes = [
    Index(name = "idx_content_url", columnList = "url")
  ]
)
open class ContentEntity : EntityWithUUID() {
  fun getContentOfMime(mime: String): String? {
    return if (mime == this.contentRawMime) {
      StringUtils.trimToNull(this.contentRaw)
    } else {
      null
    }
  }

  companion object {
    const val LEN_TITLE = 256
    const val LEN_URL = 1000
  }

  @Basic
  @Column(nullable = false, length = LEN_URL)
  open lateinit var url: String

  @Basic
  @Column(nullable = false)
  open var hasAudio: Boolean = false

  @Basic
  @Column(nullable = false)
  open var hasVideo: Boolean = false

  @Basic
  @Column(nullable = false, length = LEN_TITLE)
  open var title: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, NativeFeedEntity.LEN_TITLE)
    }

  @Basic
  @Column(length = LEN_TITLE)
  open var contentTitle: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, NativeFeedEntity.LEN_TITLE)
    }

  @Basic
  @Column
  open var contentRawMime: String? = null

  @Column(columnDefinition = "TEXT")
  @Basic(fetch = FetchType.LAZY)
  open var contentRaw: String? = null

  @Column(columnDefinition = "TEXT")
  open var contentText: String? = null

  @Column(nullable = false, columnDefinition = "TEXT")
  open var description: String? = null

  @Basic
  @Column(nullable = false)
  open var hasFulltext: Boolean = false

//  @Basic
//  @Column(name = "has_event", nullable = false)
//  open var hasEvent: Boolean = false

  @Basic
  @Column(length = LEN_URL)
  open var imageUrl: String? = null

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var contentSource: ArticleSource = ArticleSource.FEED

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

  @OneToMany(
    fetch = FetchType.LAZY,
    cascade = [CascadeType.REMOVE],
    mappedBy = "contentId",
    orphanRemoval = true,
    targetEntity = AttachmentEntity::class
  )
  open var attachments: List<AttachmentEntity> = emptyList()

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "from")
  open var hyperLink: MutableList<HyperLinkEntity> = mutableListOf()

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "content")
  open var harvestTask: HarvestTaskEntity? = null

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.DETACH], mappedBy = "content")
  open var articles: MutableList<ArticleEntity> = mutableListOf()

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

}

