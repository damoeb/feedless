package org.migor.feedless.plan

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.license.LicenseEntity
import org.migor.feedless.user.UserEntity
import java.time.LocalDateTime
import java.util.*

enum class OrderStatus {
  ACTIVE,
  CANCELED
}

@Entity
@Table(
  name = "t_order",
)
open class OrderEntity : EntityWithUUID() {

  @Column(name = "due_to")
  open var dueTo: LocalDateTime? = null

  @Column(name = "price", nullable = false)
  @Min(0)
  open var price: Double = 0.0

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  open var status: OrderStatus = OrderStatus.ACTIVE

  @Column(name = "target_group_individual", nullable = false)
  open var targetGroupIndividual: Boolean = false

  @Column(name = "target_group_enterprise", nullable = false)
  open var targetGroupEnterprise: Boolean = false

  @Column(name = "target_group_other", nullable = false)
  open var targetGroupOther: Boolean = false

  @Column(name = "invoice_recipient_name", nullable = false, length = 150)
  open lateinit var invoiceRecipientName: String

  @Column(name = "invoice_recipient_email", nullable = false, length = 150)
  open lateinit var invoiceRecipientEmail: String

  @NotEmpty
  @Column(name = "callback_url", nullable = false, length = 300)
  open var callbackUrl: String = ""

  @Column(name = "payment_method", length = 50)
  @Enumerated(EnumType.STRING)
  open var paymentMethod: PaymentMethod? = null

  @Column(name = "paid_from")
  open var paidFrom: LocalDateTime? = null

  @Column(name = "paid_until")
  open var paidUntil: LocalDateTime? = null

  @Column(name = "stripe_session_id")
  open var stripeSessionId: String? = null

  @Column(name = "stripe_subscription_id")
  open var stripeSubscriptionId: String? = null

  @Column(name = "stripe_status")
  open var stripeStatus: String? = null

  @Column(name = "stripe_last_synced_at")
  open var stripeLastSyncedAt: LocalDateTime? = null

  @Column(name = StandardJpaFields.product_id, nullable = false)
  open var productId: UUID? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.product_id,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_order__to__product")
  )
  open var product: ProductEntity? = null

  @Column(name = StandardJpaFields.userId, nullable = false)
  open lateinit var userId: UUID

  @ManyToOne(fetch = FetchType.LAZY)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JoinColumn(
    name = StandardJpaFields.userId,
    referencedColumnName = "id",
    insertable = false,
    updatable = false,
    foreignKey = ForeignKey(name = "fk_order__to__user")
  )
  open var user: UserEntity? = null

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var licenses: MutableList<LicenseEntity> = mutableListOf()

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "orderId")
  @OnDelete(action = OnDeleteAction.NO_ACTION)
  open var invoices: MutableList<InvoiceEntity> = mutableListOf()
}
