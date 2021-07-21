package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "t_source_error")
class SourceError() {
  constructor(message: String, source: Source) : this() {
    this.message = message
    this.source = source
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Lob
  var message: String? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "source_id")
  var source: Source? = null

  @Column(name = "source_id",
    updatable = false, insertable = false)
  var sourceId: String? = null

  @Temporal(TemporalType.TIMESTAMP)
  var createdAt = Date()

}
