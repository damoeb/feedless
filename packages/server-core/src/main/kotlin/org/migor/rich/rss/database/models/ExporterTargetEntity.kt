package org.migor.rich.rss.database.models

import org.migor.rich.rss.database.enums.ExporterTargetType
import org.migor.rich.rss.database.EntityWithUUID
import java.util.*
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "t_exporter_target")
open class ExporterTargetEntity: EntityWithUUID() {

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  open var type: ExporterTargetType? = null

  @Basic
  @Column(name = "exporterId", nullable = true, insertable = false, updatable = false)
  open var exporterId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exporterId", referencedColumnName = "id")
  open var exporter: ExporterEntity? = null

}
