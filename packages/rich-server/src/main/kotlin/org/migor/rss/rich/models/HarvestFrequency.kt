package org.migor.rss.rich.models

import org.hibernate.annotations.GenericGenerator
import java.util.concurrent.TimeUnit
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id


@Entity
class HarvestFrequency {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var uuid: String? = null

  @Column(nullable = false)
  var timeUnit: TimeUnit? = null

  @Column(nullable = false)
  var interval: Int? = null

}
