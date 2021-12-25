package org.migor.rss.rich.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rss.rich.util.JsonUtil
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull

@Entity
@Table(name = "\"User\"")
class User {
  fun toJson(): String {
    return JsonUtil.gson.toJson(this)
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @NotNull
  @Column(name = "\"email\"")
  var email: String? = null

  @Column(name = "\"name\"")
  var name: String? = null

//  @OneToMany(cascade = [CascadeType.ALL], mappedBy = "owner")
//  lateinit var buckets: Set<Bucket>
}
