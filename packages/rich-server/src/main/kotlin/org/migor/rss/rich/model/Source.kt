package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SourceDto
import org.springframework.validation.annotation.Validated
import java.time.temporal.ChronoUnit
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Validated
@Table(name = "t_source")
class Source() {
  constructor(url: String) : this() {
    this.url = url
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var url: String? = null

  @Column
  @NotNull
  @Enumerated(EnumType.STRING)
  var retentionPolicy: EntryRetentionPolicy = EntryRetentionPolicy.MINIMAL

  @Column
  @NotNull
  var withFulltext: Boolean = false

  @Column
  var title: String? = null

  @Lob
  var description: String? = null

  @Basic
  var copyright: String? = null

  @Basic
  var language: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var lastUpdatedAt: Date = Date()

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var sourceType: SourceType = SourceType.NATIVE

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var status: SourceStatus = SourceStatus.ACTIVE

  @Basic
  @NotNull
  var harvestTimeUnit: ChronoUnit = ChronoUnit.HOURS

  @Basic
  @NotNull
  var harvestIntervalValue: Long = 2

  @Temporal(TemporalType.TIMESTAMP)
  var nextHarvestAt: Date = Date()

  fun toDto() = SourceDto(id, title, description, status, lastUpdatedAt)

}
