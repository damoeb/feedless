package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.HarvestFrequencyDto
import java.time.temporal.ChronoUnit
import javax.persistence.*


@Entity
@Table(name = "t_harvest_freq")
class HarvestFrequency() {
  constructor(intervalValue: Int, timeUnit: ChronoUnit): this() {
    this.intervalValue = intervalValue
    this.timeUnit = timeUnit
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var timeUnit: ChronoUnit? = null

  @Column(nullable = false)
  var intervalValue: Int? = null

  fun toDto() = HarvestFrequencyDto(id, timeUnit, intervalValue)

}
