package org.migor.rich.rss.database.models

import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.models.ContentEntity.Companion.LEN_TITLE
import javax.persistence.Basic
import javax.persistence.Cacheable
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.Index
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "t_web_document", indexes = [
  Index(name = "idx_web_document_url", columnList = "url"),
])
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
open class WebDocumentEntity : EntityWithUUID() {

  companion object {
    const val LEN_URL = 500
    const val LEN_DESCRIPTION = 500

  }

  @Basic
  @Column(nullable = false, length = LEN_URL, unique = true)
  open var url: String? = null
    set(value) {
      if (StringUtils.length(value) > LEN_URL) {
        throw IllegalArgumentException("Url $value too long")
      }
      field = value
    }

  @Basic
  @Column(length = LEN_TITLE)
  open var title: String? = null
    set(value) {
      if (StringUtils.length(value) > LEN_TITLE) {
        throw IllegalArgumentException("Title $value too long")
      }
      field = value
    }


  @Basic
  open var type: String? = null

  @Basic
  open var finished: Boolean = false

  @Basic
  @Column(length = LEN_DESCRIPTION)
  open var description: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_DESCRIPTION)
    }

  @Basic
  @Column(length = 400)
  open var imageUrl: String? = null
    set(value) {
      if (StringUtils.length(value) > 400) {
        throw IllegalArgumentException("image $value too long")
      }
      field = value
    }

  @Basic
  @Column(nullable = false)
  open var score: Double = 0.0
  override fun toString(): String {
    return "WebDocumentEntity(url=$url, title=$title, type=$type, description=$description, image=$imageUrl)"
  }

  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "to")
  open var hyperLink: MutableList<HyperLinkEntity> = mutableListOf()

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "webDocument")
  open var harvestTask: HarvestTaskEntity? = null
}
