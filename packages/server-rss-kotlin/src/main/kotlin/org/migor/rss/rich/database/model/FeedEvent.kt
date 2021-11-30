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
@Table(name = "\"FeedEvent\"")
class FeedEvent(): JsonSupport() {
  constructor(message: String?, feed: Feed, error: Boolean) : this() {
    this.message = message
    this.feedId = feed.id
    this.error = error
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "message")
  var message: String? = null

  @NotNull
  @Column(name = "\"feedId\"")
  var feedId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"createdAt\"")
  var createdAt: Date = Date()

  @Column(name = "is_error")
  var error: Boolean = false
}
