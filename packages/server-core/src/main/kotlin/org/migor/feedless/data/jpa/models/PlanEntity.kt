package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.generated.types.Plan

enum class PlanAvailability {
  available,
  by_request,
  unavailable
}

enum class PlanName {
  internal,
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

  @ManyToMany(fetch = FetchType.LAZY)
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
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var features: MutableList<FeatureEntity> = mutableListOf()
}

fun PlanEntity.toDto(): Plan {
  return Plan.newBuilder()
    .id(id.toString())
    .costs(costs)
    .name(name.toDto())
    .availability(availability.toDto())
    .isPrimary(primary)
    .build()
}

private fun PlanName.toDto(): org.migor.feedless.generated.types.PlanName = when (this) {
  PlanName.free -> org.migor.feedless.generated.types.PlanName.free
  PlanName.basic -> org.migor.feedless.generated.types.PlanName.basic
  else -> throw RuntimeException("cannot be exported")
}

private fun PlanAvailability.toDto(): org.migor.feedless.generated.types.PlanAvailability = when (this) {
  PlanAvailability.by_request -> org.migor.feedless.generated.types.PlanAvailability.by_request
  PlanAvailability.available -> org.migor.feedless.generated.types.PlanAvailability.available
  PlanAvailability.unavailable -> throw RuntimeException("cannot be exported")
}
