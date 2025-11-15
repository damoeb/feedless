package org.migor.feedless.data.jpa.license

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.order.OrderEntity
import java.util.*

@Entity
@Table(
  name = "t_license",
)
open class LicenseEntity : EntityWithUUID() {

  @Column(name = "payload", nullable = false, length = 1000)
  open lateinit var payload: String

  @Column(name = StandardJpaFields.order_id, nullable = false)
  open var orderId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.order_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_license__to__order")
  )
  open var order: OrderEntity? = null
}
