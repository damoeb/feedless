package org.migor.rich.rss.data.jpa.models

import jakarta.persistence.Basic
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table
import org.migor.rich.rss.data.jpa.EntityWithUUID

@Entity
@Table(name = "t_feature")
open class FeatureEntity : EntityWithUUID() {

  @Basic
  @Column(nullable = false)
  open lateinit var name: FeatureName

  @Basic
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  open var featureType: FeatureType = FeatureType.OFF

  @Basic
  open var valueInt: Int? = null

  @Basic
  open var valueBool: Boolean? = null

  @Basic
  open var valueDouble: Double? = null

  @Basic
  open var valueString: String? = null

}

enum class FeatureName {
  genFeedFromWebsite,
  genFeedFromFeed,
  genFeedFromPageChange,
  refreshRate,
  inlineImages,
  prerendering,
  unqureShortenedLinks,

}

enum class FeatureType {
  OFF,
  BOOL,
  INT,
  DOUBLE,
  STRING
}

