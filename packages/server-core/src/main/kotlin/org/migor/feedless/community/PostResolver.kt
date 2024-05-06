package org.migor.feedless.community

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.generated.types.Comment
import org.migor.feedless.generated.types.CreateCommentInput
import org.migor.feedless.generated.types.CreateStoryInput
import org.migor.feedless.generated.types.StoriesWhereInput
import org.migor.feedless.generated.types.Story
import org.migor.feedless.util.CryptUtil
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestHeader

@DgsComponent
@Profile(AppProfiles.community)
class PostResolver {

  private val log = LoggerFactory.getLogger(PostResolver::class.simpleName)

  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  suspend fun createStory(
    @RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
    dfe: DataFetchingEnvironment,
    @InputArgument data: CreateStoryInput,
  ): Story = coroutineScope {
    val corrId = CryptUtil.handleCorrId(corrIdParam)
    log.info("[$corrId] createStory")

    Story.newBuilder().build()
  }

  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  suspend fun createComment(
    @RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
    dfe: DataFetchingEnvironment,
    @InputArgument data: CreateCommentInput,
  ): Comment = coroutineScope {
    val corrId = CryptUtil.handleCorrId(corrIdParam)
    log.info("[$corrId] createComment")

    Comment.newBuilder().build()
  }

  @DgsQuery
  suspend fun stories(
    @RequestHeader(ApiParams.corrId, required = false) corrIdParam: String,
    dfe: DataFetchingEnvironment,
    @InputArgument data: StoriesWhereInput,
  ): List<Story> = coroutineScope {
    val corrId = CryptUtil.handleCorrId(corrIdParam)
    log.info("[$corrId] stories")

    emptyList()
  }

}
