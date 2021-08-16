package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.springframework.data.annotation.CreatedDate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "ReleaseThrottle")
class ReleaseThrottle {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "take")
  var take: Int = 10

  @NotNull
  @Column(name = "window")
  var window: String = "d"

  @NotNull
  @Column(name = "scoreCriteria")
  var scoreCriteria: String? = null

  @NotNull
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "nextReleaseAt")
  var nextReleaseAt: Date? = null

  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date = Date()

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updatedAt")
  var updatedAt: Date = Date()

}
