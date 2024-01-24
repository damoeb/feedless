package org.migor.feedless.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.Product
import org.migor.feedless.generated.types.Plan

enum class PlanAvailability {
  available,
  by_request,
  unavailable
}

enum class PlanName {
  internal,
  free,
  basic,
  pro
}


@Entity
@Table(name = "t_plan", uniqueConstraints = [
  UniqueConstraint(name = "UniquePlanNamePerProduct", columnNames = [StandardJpaFields.name, StandardJpaFields.product])]
)
open class PlanEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, name = StandardJpaFields.name)
  @Enumerated(EnumType.STRING)
  open lateinit var name: PlanName

  @Basic
  @Column(nullable = false, name = StandardJpaFields.product)
  @Enumerated(EnumType.STRING)
  open lateinit var product: Product

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var availability: PlanAvailability

  @Basic
  @Column(nullable = false)
  open var currentCosts: Double = 0.0

  @Basic
  open var beforeCosts: Double? = null

//  @Basic
//  open var beforeCosts: Double? = null

  @Basic
  @Column(nullable = false)
  open var primaryPlan: Boolean = false

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "id")
//  @JoinTable(
//    name = "map_plan_to_feature",
//    joinColumns = [
//      JoinColumn(
//        name = "plan_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )],
//    inverseJoinColumns = [
//      JoinColumn(
//        name = "feature_id", referencedColumnName = "id",
//        nullable = false, updatable = false
//      )
//    ]
//  )
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var features: MutableList<FeatureEntity> = mutableListOf()
}

fun PlanEntity.toDto(): Plan {
  return Plan.newBuilder()
    .id(id.toString())
    .currentCosts(currentCosts)
    .beforeCosts(beforeCosts)
    .name(name.toDto())
    .availability(availability.toDto())
    .isPrimary(primaryPlan)
    .features(features.map { it.toDto() })
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
