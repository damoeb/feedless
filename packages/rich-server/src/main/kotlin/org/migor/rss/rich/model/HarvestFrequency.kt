package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.HarvestFrequencyDto
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
  var intervalValue: Int? = null

  fun toDto() = HarvestFrequencyDto(uuid, timeUnit, intervalValue)

}
