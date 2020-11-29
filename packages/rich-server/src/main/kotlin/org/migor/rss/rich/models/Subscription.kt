package org.migor.rss.rich.models

import javax.persistence.*

@Entity
@Table(name = "ubscription")
class Subscription {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  val id: Long? = null

}
