package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "NoFollowUrl")
class NoFollowUrl {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "url_prefix")
  var url: String? = null
}
