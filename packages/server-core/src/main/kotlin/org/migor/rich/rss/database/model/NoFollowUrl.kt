package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import org.springframework.context.annotation.Profile
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Profile("stateful")
@Entity
@Table(name = "\"NoFollowUrl\"")
class NoFollowUrl {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "url_prefix")
  var url: String? = null
}
