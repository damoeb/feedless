package org.migor.feedless.data.jpa.auction

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "t_pending_auction_alert")
class PendingAuctionAlertEntity(
  @Id
  @Column(nullable = false)
  val id: UUID = UUID.randomUUID(),
  @Column(name = "repository_id", nullable = false)
  val repositoryId: UUID,
  @Column(nullable = false)
  val email: String,
  @Lob
  @Column(name = "segmentation_json", nullable = false)
  val segmentationJson: String,
  @Column(nullable = false)
  var status: String = "PENDING",
  @Column(name = "created_at", nullable = false)
  val createdAt: LocalDateTime = LocalDateTime.now(),
)

object PendingAuctionAlertStatus {
  const val pending = "PENDING"
  const val completed = "COMPLETED"
}
