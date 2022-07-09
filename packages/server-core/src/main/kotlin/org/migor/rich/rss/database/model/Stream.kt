package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import org.springframework.context.annotation.Profile
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Profile("database")
@Entity
@Table(name = "\"Stream\"")
class Stream {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null
}
