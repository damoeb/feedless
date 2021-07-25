package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "Subscription")
class Subscription {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "tags", columnDefinition = "JSON")
  var tags: String? = null

  @NotNull
  @Column(name = "ownerId")
  var ownerId: String? = null

  @NotNull
  @Column(name = "bucketId")
  var bucketId: String? = null

  @NotNull
  @Column(name = "throttleId")
  var throttleId: String? = null

  @NotNull
  @Column(name = "feedId")
  var feedId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updatedAt")
  var updatedAt: Date? = null

}
