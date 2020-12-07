package org.migor.rss.rich.model

import com.google.gson.GsonBuilder
import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.EntryDto
import java.util.*
import javax.persistence.*

object EntryUtil {
  val gson = GsonBuilder().create()
}

@Entity
@Table(name = "t_entry")
class Entry {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Lob
  var content: String? = null

  @Column(nullable = false)
  var link: String? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subscription_id")
  var subscription: Subscription? = null

  @Column(name = "subscription_id",
    updatable = false, insertable = false)
  var subscriptionId: String? = null

  @Basic
  var createdAt = Date()

  fun toDto(): EntryDto? {
    val entryDto = EntryUtil.gson.fromJson(content, EntryDto::class.java)
    id?.let { entryDto.put("id", it) }
    return entryDto
  }
}
