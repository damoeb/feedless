package org.migor.rich.rss.database.model

import org.hibernate.annotations.GenericGenerator
import org.migor.rich.rss.database.enums.ExporterTargetType
import org.springframework.context.annotation.Profile
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Table

@Profile("database")
@Entity
@Table(name = "\"ArticleExporterTarget\"")
class ExporterTarget : JsonSupport() {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  var type: ExporterTargetType? = null

//  @Column(name = "context")
//  @Type(type = "jsonb")
//  @Basic(fetch = FetchType.LAZY)
//  var context: Map<String, Any>? = null

  @Column(name = "\"exporterId\"")
  var exporterId: String? = null
}
