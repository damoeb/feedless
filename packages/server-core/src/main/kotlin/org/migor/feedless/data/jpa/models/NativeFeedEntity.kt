package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.apache.commons.lang3.StringUtils
import org.hibernate.annotations.Type
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.enums.NativeFeedStatus
import org.slf4j.LoggerFactory
import java.util.*

@Entity
@Table(
  name = "t_feed_native",
  uniqueConstraints = [
    UniqueConstraint(name = "UniqueOwnerAndFeed", columnNames = [StandardJpaFields.ownerId, StandardJpaFields.feedUrl])]
)
open class NativeFeedEntity : EntityWithUUID() {

  @Transient
  private val log = LoggerFactory.getLogger(NativeFeedEntity::class.simpleName)

  companion object {
    const val LEN_TITLE = 256
    const val LEN_ERROR_MESSAGE = 256
    const val LEN_DESCRIPTION = 1024
    const val LEN_URL = 1000
  }

  @Basic
  open var domain: String? = null

  @Basic
  @Column(length = LEN_ERROR_MESSAGE)
  open var errorMessage: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_ERROR_MESSAGE)
      logLengthViolation("errorMessage", value, field)
    }

  @Basic
  open var websiteUrl: String? = null

  @Basic
  open var imageUrl: String? = null

  @Basic
  open var iconUrl: String? = null

  @Basic
  open var lang: String? = null

  @Basic
  @Column(name = StandardJpaFields.visibility, nullable = false)
  @Enumerated(EnumType.STRING)
  open var visibility: EntityVisibility = EntityVisibility.isPublic

  @Basic
  @Column(name = StandardJpaFields.ownerId, nullable = false)
  open lateinit var ownerId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = StandardJpaFields.ownerId, referencedColumnName = StandardJpaFields.id, insertable = false, updatable = false)
  open var owner: UserEntity? = null

  @Basic
  @Column(name = StandardJpaFields.feedUrl, nullable = false, length = LEN_URL)
  open lateinit var feedUrl: String

  @Basic
  @Column(name = StandardJpaFields.title, nullable = false, length = LEN_TITLE)
  open var title: String = ""
    set(value) {
      field = StringUtils.substring(value, 0, LEN_TITLE)
      logLengthViolation("title", value, field)
    }

  @Basic
  @Column(length = LEN_DESCRIPTION)
  open var description: String? = null
    set(value) {
      field = StringUtils.substring(value, 0, LEN_DESCRIPTION)
      logLengthViolation("description", value, field)
    }


  @Basic
  open var harvestIntervalMinutes: Int? = null

  @Basic
  @Column(nullable = false)
  open var harvestRateFixed: Boolean = false

  @Basic
  open var nextHarvestAt: Date? = null

  @Basic
  open var retentionSize: Int? = null

  @Basic
  @Column(nullable = false)
  open var harvestSiteWithPrerender: Boolean = false

  @Basic
  open var lastCheckedAt: Date? = null

  @Basic
  open var lastChangedAt: Date? = null

  @Basic
  @Column(nullable = false)
  open var failedAttemptCount: Int = 0

  @Basic
  open var lat: Long? = null

  @Basic
  open var lon: Long? = null

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: NativeFeedStatus = NativeFeedStatus.NEVER_FETCHED

  @OneToOne(fetch = FetchType.LAZY, orphanRemoval = true, cascade = [CascadeType.ALL], optional = true)
  @JoinColumn(name = "generic_feed_id", referencedColumnName = "id")
  open var genericFeed: GenericFeedEntity? = null

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open lateinit var plugins: List<String>

//  @Column(name = "generic_feed_id", insertable = false, updatable = false)
//  @Basic
//  open var genericFeedId: UUID? = null

  @Basic
  @Column(name = "streamId", nullable = false)
  open lateinit var streamId: UUID

  @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "streamId", referencedColumnName = "id", insertable = false, updatable = false)
  open var stream: StreamEntity? = null

  @OneToMany(mappedBy = "id", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  open var importers: MutableList<ImporterEntity>? = mutableListOf()

  private fun logLengthViolation(field: String, expectedValue: String?, actualValue: String?) {
    actualValue?.let {
      if (expectedValue!!.length != actualValue.length) {
        log.warn("Persisted value for '${field}' transformed from '$expectedValue' -> '${actualValue}'")
      }
    }
  }

  @PrePersist
  fun prePersist() {
    retentionSize?.let {
      retentionSize = it.coerceAtLeast(1)
    }
  }

}
