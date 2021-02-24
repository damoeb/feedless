package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SourceDto
import org.springframework.validation.annotation.Validated
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

  @Column(nullable = false, unique = true)
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
  var lang: String? = null

  @Basic
  var siteUrl: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var updatedAt: Date? = null

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var sourceType: SourceType = SourceType.NATIVE

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  var status: SourceStatus = SourceStatus.ACTIVE

  @Basic
  @NotNull
  var harvestIntervalMinutes: Long = 10

  @Temporal(TemporalType.TIMESTAMP)
  var nextHarvestAt: Date = Date()

  fun toDto() = SourceDto(id, title, description, status, updatedAt, url)

}
