package org.migor.feedless.api.graphql

import graphql.GraphQLError
import org.springframework.graphql.execution.SubscriptionExceptionResolverAdapter
import org.springframework.stereotype.Component

/** Publisher errors bypass DataFetcherExceptionHandler; without this the client only sees "Subscription error". */
@Component
class GraphQLSubscriptionExceptionResolver : SubscriptionExceptionResolverAdapter() {

  override fun resolveToSingleError(exception: Throwable): GraphQLError = toGraphQLError(exception, null)
}
