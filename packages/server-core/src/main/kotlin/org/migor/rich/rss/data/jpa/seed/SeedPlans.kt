package org.migor.rich.rss.data.jpa.seed

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.data.jpa.repositories.FeatureDAO
import org.migor.rich.rss.data.jpa.repositories.PlanDAO
import org.migor.rich.rss.service.PropertyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Service

@Service
@Order(1)
@Profile("${AppProfiles.bootstrap} && ${AppProfiles.database}")
class SeedPlans {

  @Autowired
  lateinit var planDAO: PlanDAO

  @Autowired
  lateinit var featureDAO: FeatureDAO

  @Autowired
  lateinit var propertyService: PropertyService

//  @PostConstruct
//  @Transactional(propagation = Propagation.REQUIRED)
//  fun postConstruct() {
//    val personal = toPlan("Personal", 0.0, PlanAvailability.available)
////    val basic = toPlan("Basic", 0.0, PlanAvailability.available)
//    val professional = toPlan("Professional", 0.0, PlanAvailability.available)
//    val enterprise = toPlan("Enterprise", 0.0, PlanAvailability.by_request)
//  }
//
//  private fun toPlan(name: String, costs: Double, availability: PlanAvailability): PlanEntity {
//    val plan = PlanEntity()
//    plan.name = "Personal"
//    plan.costs = 0.0
//    plan.availability = PlanAvailability.available
//
//    return plan
//  }

//  private fun features() {
//    val features = listOf(
//      toFeature("fulltext"),
//      toFeature("digest")
//    )
//  }
//
//  private fun toFeature(name: String): FeatureEntity {
//
//  }

}
