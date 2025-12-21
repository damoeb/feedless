package org.migor.feedless.feed

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.withContext
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.throttle.Throttled
import org.migor.feedless.auth.AuthToken
import org.migor.feedless.generated.DgsConstants
import org.migor.feedless.session.createRequestContext
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import java.net.URI
import org.migor.feedless.generated.types.Authentication as AuthenticationDto


@DgsComponent
@Profile("${AppProfiles.feed} & ${AppLayer.api}")
class FeedResolver(
  private val feedService: FeedService,
) {

  private val log = LoggerFactory.getLogger(FeedResolver::class.simpleName)

  @Throttled
  @DgsMutation(field = DgsConstants.MUTATION.IssueAnonymousFeedToken)
  @PreAuthorize("@capabilityService.hasToken()")
  suspend fun issueAnonymousFeedToken(
    dfe: DataFetchingEnvironment,
    @InputArgument(DgsConstants.MUTATION.ISSUEANONYMOUSFEEDTOKEN_INPUT_ARGUMENT.Url) url: String,
  ): AuthenticationDto = withContext(context = createRequestContext()) {
    log.debug("createAnonymousFeedUrl $url")
    feedService.createAnonymousFeedUrl(URI(url)).toDto()
  }
}

fun AuthToken.toDto(): AuthenticationDto {
  return AuthenticationDto(
    corrId = "",
    token = token
  )
}

