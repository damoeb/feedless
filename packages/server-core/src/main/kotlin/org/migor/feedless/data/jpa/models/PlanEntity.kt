package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.migor.feedless.data.jpa.EntityWithUUID

enum class PlanAvailability {
  available,
  by_request,
  unavailable
}

enum class PlanName {
  free,
  basic
}


@Entity
@Table(name = "t_plan")
open class PlanEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, unique = true)
  @Enumerated(EnumType.STRING)
  open lateinit var name: PlanName

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var availability: PlanAvailability

  @Basic
  @Column(nullable = false)
  open var costs: Double = 0.0

  @Basic
  @Column(nullable = false, name = "is_primary")
  open var primary: Boolean = false

  @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
  @JoinTable(
    name = "map_plan_to_feature",
    joinColumns = [
      JoinColumn(
        name = "plan_id", referencedColumnName = "id",
        nullable = false, updatable = false
      )],
    inverseJoinColumns = [
      JoinColumn(
        name = "feature_id", referencedColumnName = "id",
        nullable = false, updatable = false
      )
    ]
  )
  open var features: MutableList<FeatureEntity> = mutableListOf()
}

