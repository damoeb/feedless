package org.migor.feedless.report

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.data.jpa.auction.PendingAuctionAlertDAO
import org.migor.feedless.data.jpa.auction.PendingAuctionAlertEntity
import org.migor.feedless.data.jpa.auction.PendingAuctionAlertStatus
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.payment.PaymentUseCase
import org.migor.feedless.payment.PendingCheckoutFinalizer
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.config.AuctionAlertProperties
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile("${AppProfiles.report} & ${AppLayer.service}")
class AuctionAlertCheckoutUseCase(
  private val pendingAuctionAlertDAO: PendingAuctionAlertDAO,
  private val userRepository: UserRepository,
  private val userUseCase: UserUseCase,
  private val reportUseCase: ReportUseCase,
  private val groupRepository: GroupRepository,
  private val auctionAlertProperties: AuctionAlertProperties,
) : PendingCheckoutFinalizer {

  private val log = LoggerFactory.getLogger(AuctionAlertCheckoutUseCase::class.java)
  private val objectMapper = jacksonObjectMapper()

  @Autowired(required = false)
  private var paymentUseCase: PaymentUseCase? = null

  data class CheckoutStartResult(
    val checkoutUrl: String?,
    val loginRequired: Boolean,
    val errorMessage: String?,
  )

  suspend fun startCheckout(repositoryId: RepositoryId, segment: SegmentInput): CheckoutStartResult = withContext(Dispatchers.IO) {
    val email = segment.recipient.email.email.trim().lowercase()
    if (userRepository.existsByEmail(email)) {
      return@withContext CheckoutStartResult(
        checkoutUrl = null,
        loginRequired = true,
        errorMessage = "Für diese E-Mail existiert bereits ein Konto. Bitte melden Sie sich an.",
      )
    }

    val payment = paymentUseCase
      ?: return@withContext CheckoutStartResult(
        checkoutUrl = null,
        loginRequired = false,
        errorMessage = "Zahlungen sind derzeit nicht konfiguriert.",
      )

    val stripe = auctionAlertProperties.stripe
    val checkout = auctionAlertProperties.checkout
    if (stripe.priceId.isBlank() || checkout.successUrl.isBlank() || checkout.cancelUrl.isBlank()) {
      log.warn("auction-alert checkout not configured (price-id or URLs)")
      return@withContext CheckoutStartResult(
        checkoutUrl = null,
        loginRequired = false,
        errorMessage = "Checkout ist nicht konfiguriert.",
      )
    }

    val pendingId = UUID.randomUUID()
    val json = objectMapper.writeValueAsString(segment)

    pendingAuctionAlertDAO.save(
      PendingAuctionAlertEntity(
        id = pendingId,
        repositoryId = repositoryId.uuid,
        email = email,
        segmentationJson = json,
        status = PendingAuctionAlertStatus.pending,
      )
    )

    val session = payment.createSubscriptionCheckoutSession(
      priceId = stripe.priceId,
      successUrl = checkout.successUrl,
      cancelUrl = checkout.cancelUrl,
      trialPeriodDays = checkout.trialDays,
      customerEmail = email,
      metadata = mapOf("pendingId" to pendingId.toString()),
    )

    CheckoutStartResult(
      checkoutUrl = session.checkoutUrl,
      loginRequired = false,
      errorMessage = null,
    )
  }

  override suspend fun finalizePendingCheckout(pendingId: UUID) = withContext(Dispatchers.IO) {
    log.info("finalizePendingCheckout pendingId=$pendingId")
    val row = pendingAuctionAlertDAO.findById(pendingId).orElse(null)
      ?: run {
        log.warn("pending auction alert not found: $pendingId")
        return@withContext
      }
    if (row.status == PendingAuctionAlertStatus.completed) {
      log.info("pending already completed: $pendingId")
      return@withContext
    }

    val email = row.email
    val segment: SegmentInput = objectMapper.readValue(row.segmentationJson)

    val user = userRepository.findByEmail(email)
      ?: try {
        userUseCase.createUser(email)
      } catch (e: BadRequestException) {
        if (e.message?.contains("already exists") == true) {
          userRepository.findByEmail(email) ?: throw e
        } else {
          throw e
        }
      }

    val groupId = groupRepository.findAllByOwner(user.id).firstOrNull()?.id
      ?: throw IllegalStateException("user ${user.id} has no group")

    reportUseCase.createReportForPublicRepository(
      RepositoryId(row.repositoryId),
      segment,
      user.id,
    )

    row.status = PendingAuctionAlertStatus.completed
    pendingAuctionAlertDAO.save(row)
  }
}
