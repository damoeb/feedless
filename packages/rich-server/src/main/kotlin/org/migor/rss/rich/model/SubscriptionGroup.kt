package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.dto.SourceEntryDto
import org.migor.rss.rich.dto.SubscriptionGroupDto
import org.springframework.validation.annotation.Validated
import javax.persistence.*
import javax.validation.constraints.NotNull


@Entity
@Validated
@Table(name = "t_subscription_group")
class SubscriptionGroup() {
  constructor(name: String, owner: User) : this() {
    this.name = name
    this.owner = owner
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column
  @NotNull
  var name: String? = null

  @Basic
  var priority: Int? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id")
  var owner: User? = null

  @Column(name = "owner_id",
    updatable = false, insertable = false)
  var ownerId: String? = null

  fun toDto(entries: List<SourceEntryDto?>? = null): SubscriptionGroupDto {
    return SubscriptionGroupDto(id, name, ownerId, priority, null, entries)
  }
}
