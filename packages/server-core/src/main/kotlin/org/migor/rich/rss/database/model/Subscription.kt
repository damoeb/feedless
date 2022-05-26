package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.Type
import org.springframework.context.annotation.Profile
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.Temporal
import javax.persistence.TemporalType
import javax.validation.constraints.NotNull

@Profile("stateful")
@Entity
@Table(name = "\"Subscription\"")
class Subscription : JsonSupport() {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(name = "tags", columnDefinition = "JSONB")
  @Type(type = "jsonb")
  @Basic(fetch = FetchType.LAZY)
  var tags: Array<String>? = null

  @NotNull
  @Column(name = "\"ownerId\"")
  var ownerId: String? = null

  @Column(name = "title")
  var name: String? = null

  @NotNull
  @Column(name = "\"bucketId\"")
  var bucketId: String? = null

  @NotNull
  @Column(name = "\"feedId\"")
  var feedId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "\"lastUpdatedAt\"")
  var lastUpdatedAt: Date? = null

}
