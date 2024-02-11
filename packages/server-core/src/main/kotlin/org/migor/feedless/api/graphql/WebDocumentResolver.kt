package org.migor.feedless.api.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsMutation
import com.netflix.graphql.dgs.InputArgument
import kotlinx.coroutines.coroutineScope
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiParams
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.generated.types.DeleteWebDocumentInput
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.WebDocumentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestHeader
import java.util.*

@DgsComponent
@Profile(AppProfiles.database)
class WebDocumentResolver {

  private val log = LoggerFactory.getLogger(WebDocumentResolver::class.simpleName)

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var webDocumentService: WebDocumentService

  @Autowired
  lateinit var currentUser: CurrentUser

//  @DgsMutation
//  @PreAuthorize("hasAuthority('USER')")
//  @Transactional(propagation = Propagation.REQUIRED)
//  suspend fun createUserSecret(
//    @RequestHeader(ApiParams.corrId) corrId: String,
//  ): UserSecret = coroutineScope {
//    userSecretService.createUserSecret(corrId, currentUser.user(corrId)).toDto(false)
//  }

  @DgsMutation
  @PreAuthorize("hasAuthority('USER')")
  @Transactional(propagation = Propagation.REQUIRED)
  suspend fun deleteWebDocument(
    @InputArgument data: DeleteWebDocumentInput,
    @RequestHeader(ApiParams.corrId) corrId: String,
  ): Boolean = coroutineScope {
    webDocumentService.deleteWebDocumentById(corrId, currentUser.user(corrId), UUID.fromString(data.where.id))
    true
  }

}
