package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.database.enums.FeedStatus
import org.migor.rss.rich.util.JsonUtil
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "Stream")
class Stream {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null
}
