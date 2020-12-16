package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.HarvestFrequencyDto
import java.time.temporal.ChronoUnit
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "t_harvest_freq")
class HarvestFrequency {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Basic
  @NotNull
  var timeUnit: ChronoUnit = ChronoUnit.HOURS

  @Basic
  @NotNull
  var intervalValue: Long = 2

  fun toDto() = HarvestFrequencyDto(id!!, timeUnit, intervalValue)

}
