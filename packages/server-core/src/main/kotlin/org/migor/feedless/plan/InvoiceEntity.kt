package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import java.util.*

@Entity
@Table(
  name = "t_invoice",
)
open class InvoiceEntity : EntityWithUUID() {

  @Column(name = "price", nullable = false)
  @Min(0)
  open var price: Double = 0.0

  @Column(name = "is_canceled", nullable = false)
  open var isCanceled: Boolean = false

  @Column(name = "due_to")
  open var dueTo: Date? = null

  @Column(name = "paid_at")
  open var paidAt: Date? = null

  @Column(name = StandardJpaFields.order_id, nullable = false)
  open var orderId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.order_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_invoice__to__order")
  )
  open var order: OrderEntity? = null
}
