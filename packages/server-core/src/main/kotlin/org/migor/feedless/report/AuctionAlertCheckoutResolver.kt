package org.migor.feedless.report

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.generated.types.AuctionAlertCheckoutResult
import org.migor.feedless.generated.types.SegmentInput
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize

@DgsComponent
@Profile("${AppProfiles.report} & ${AppLayer.api}")
class AuctionAlertCheckoutResolver(
  private val auctionAlertCheckoutUseCase: AuctionAlertCheckoutUseCase,
) {

  private val log = LoggerFactory.getLogger(AuctionAlertCheckoutResolver::class.java)

  @Throttled
  @PreAuthorize("permitAll()")
  @DgsMutation(field = "createAuctionAlertCheckout")
  suspend fun createAuctionAlertCheckout(
    @InputArgument("repositoryId") repositoryId: String,
    @InputArgument("segmentation") segmentation: SegmentInput,
  ): AuctionAlertCheckoutResult = withContext(injectCapabilitiesFromSecurityContext()) {
    log.debug("createAuctionAlertCheckout repositoryId=$repositoryId")
    val r = auctionAlertCheckoutUseCase.startCheckout(RepositoryId(repositoryId), segmentation)
    AuctionAlertCheckoutResult(
      checkoutUrl = r.checkoutUrl,
      loginRequired = r.loginRequired,
      errorMessage = r.errorMessage,
    )
  }
}
