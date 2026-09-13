package org.migor.feedless.config

import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimplePerformantInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters
import graphql.schema.DataFetchingEnvironment
import org.migor.feedless.AppLayer
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.session.injectCapabilitiesFromSecurityContext
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

/** Nested field resolvers start deferred on a pooled thread without the security context, so the caller is captured once on the request thread. */
@Component
@Profile(AppLayer.api)
class CallerContextInstrumentation : SimplePerformantInstrumentation() {

  override fun createState(parameters: InstrumentationCreateStateParameters): InstrumentationState? {
    parameters.executionInput.graphQLContext.put(RequestContext::class.java, injectCapabilitiesFromSecurityContext())
    return null
  }
}

/** The request's caller; without a captured one (a resolver called directly, as in unit tests) the thread's security context decides. */
fun DataFetchingEnvironment.requestContext(): RequestContext =
  graphQlContext?.get<RequestContext?>(RequestContext::class.java)?.copy() ?: injectCapabilitiesFromSecurityContext()
