package org.migor.rich.rss.data.jpa.models

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
import org.migor.rich.rss.data.jpa.EntityWithUUID

@Entity
@Table(name = "t_plan")
open class PlanEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false, unique = true)
  open lateinit var name: String

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open lateinit var availability: PlanAvailability

  @Basic
  @Column(nullable = false)
  open var costs: Double = 0.0

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


//  @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE], mappedBy = "bucketId")
//  open var importers: MutableList<UserEntity> = mutableListOf()

}

enum class PlanAvailability {
  available,
  by_request,
  unavailable
}

