package org.migor.feedless.data.jpa.models

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.Type
import org.migor.feedless.data.jpa.EntityWithUUID
import java.sql.Timestamp
import java.util.*

@Entity
@Table(name = "t_user")
open class UserEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, unique = true)
  open lateinit var email: String

  @Basic
  @Column(nullable = false)
  open lateinit var name: String

  @Basic
  @Column(nullable = false)
  open var isRoot: Boolean = false

  @Basic
  @Column(nullable = false)
  open var hasApprovedTerms: Boolean = false

  @Basic
  @Column(nullable = false)
  open var locked: Boolean = false

  @Basic
  open var approvedTermsAt: Timestamp? = null

  @Basic
  open var purgeScheduledFor: Timestamp? = null

  @Basic
  @Column(name = "date_format")
  open var dateFormat: String? = null // todo make nullable=false

  @Basic
  @Column(name = "time_format", nullable = true)
  open var timeFormat: String? = null

  @Basic
  @Column(name = "notifications_stream_id", nullable = false)
  open lateinit var notificationsStreamId: UUID

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "notifications_stream_id", referencedColumnName = "id", insertable = false, updatable = false)
  open var notificationsStream: StreamEntity? = null

  @Basic
  @Column(name = "plan_id", insertable = false, updatable = false)
  open var planId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
  @JoinColumn(name = "plan_id", referencedColumnName = "id")
  open var plan: PlanEntity? = null

  @Type(JsonType::class)
  @Column(columnDefinition = "jsonb", nullable = false)
  @Basic(fetch = FetchType.LAZY)
  open var plugins: MutableMap<String, Boolean> = mutableMapOf()

}

