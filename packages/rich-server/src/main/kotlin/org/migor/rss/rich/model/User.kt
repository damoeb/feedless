package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.UserDto
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "t_user")
class User {
  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(nullable = false)
  var apiKey: String? = null

  @OneToMany(targetEntity = Subscription::class, mappedBy = "owner", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
  val subscriptions: List<Subscription> = ArrayList()

  @Basic
  var createdAt = Date()

  fun toDto() = UserDto(id, apiKey, createdAt)
}
