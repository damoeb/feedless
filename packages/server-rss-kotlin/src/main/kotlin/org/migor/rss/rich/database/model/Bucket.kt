package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull

@Entity
@Table(name = "Bucket")
class Bucket {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "streamId")
  var streamId: String? = null

  @NotNull
  @Column(name = "title")
  var title: String? = null

  @NotNull
  @Column(name = "ownerId")
  var ownerId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "lastPostProcessedAt")
  var lastPostProcessedAt: Date? = null

}
