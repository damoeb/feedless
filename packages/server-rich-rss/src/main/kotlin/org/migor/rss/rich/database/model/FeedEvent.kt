package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Table(name = "FeedEvent")
class FeedEvent() {
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
  @Column(name = "message", columnDefinition = "JSON")
  var message: String? = null

  @NotNull
  @Column(name = "feedId")
  var feedId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "createdAt")
  var createdAt: Date = Date()

  @Column(name = "is_error")
  var error: Boolean = false

}
