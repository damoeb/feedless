package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.HarvestFrequencyDto
import java.util.concurrent.TimeUnit
import javax.persistence.*


@Entity
@Table(name = "t_harvest_freq")
class HarvestFrequency {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var timeUnit: TimeUnit? = null

  @Column(nullable = false)
  var intervalValue: Int? = null

  fun toDto() = HarvestFrequencyDto(id, timeUnit, intervalValue)

}
