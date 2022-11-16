package org.migor.rich.rss.database.models

import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.migor.rich.rss.database.EntityWithUUID
import org.migor.rich.rss.database.models.ContentEntity.Companion.LEN_TITLE
import java.util.*
import javax.persistence.Basic
import javax.persistence.Cacheable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Index
import javax.persistence.Table

@Entity
@Table(name = "t_web_document", indexes = [
  Index(name = "idx_web_document_url", columnList = "url")
])
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
open class WebDocumentEntity : EntityWithUUID() {

  companion object {
    const val LEN_URL = 300
    const val LEN_DESCRIPTION = 300

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
  @Column(length = LEN_DESCRIPTION)
  open var description: String? = null
    set(value) {
      if (StringUtils.length(value) > LEN_DESCRIPTION) {
        throw IllegalArgumentException("Description $value too long")
      }
      field = value
    }

  @Basic
  @Column(length = 400)
  open var image: String? = null
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
    return "WebDocumentEntity(url=$url, title=$title, type=$type, description=$description, image=$image)"
  }


}
