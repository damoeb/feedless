package org.migor.rich.rss.graphql

import com.netflix.graphql.types.errors.ErrorType
import com.netflix.graphql.types.errors.TypedGraphQLError
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.migor.rich.rss.harvest.HostOverloadingException
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture


@Component
class CustomExceptionHandler : DataFetcherExceptionHandler {
  override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters?): CompletableFuture<DataFetcherExceptionHandlerResult> {
    return when(handlerParameters?.exception) {
      is HostOverloadingException -> run {
//        val debugInfo: MutableMap<String, Any> = HashMap()
//      debugInfo["somefield"] = "somevalue"
      val graphqlError: GraphQLError = TypedGraphQLError.newInternalErrorBuilder()
        .message("HostOverloadingException")
//        .debugInfo(debugInfo)
        .errorType(ErrorType.FAILED_PRECONDITION)
        .path(handlerParameters.path).build()
      val result: DataFetcherExceptionHandlerResult = DataFetcherExceptionHandlerResult.newResult()
        .error(graphqlError)
        .build()
      CompletableFuture.completedFuture(result)
      }
      else -> super.handleException(handlerParameters)
    }
  }
  //  fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters): CompletableFuture<DataFetcherExceptionHandlerResult> {
//    return if (handlerParameters.getException() is MyException) {
//      val debugInfo: MutableMap<String, Any> = HashMap()
//      debugInfo["somefield"] = "somevalue"
//      val graphqlError: GraphQLError = TypedGraphQLError.newInternalErrorBuilder()
//        .message("This custom thing went wrong!")
//        .debugInfo(debugInfo)
//        .path(handlerParameters.getPath()).build()
//      val result: DataFetcherExceptionHandlerResult = DataFetcherExceptionHandlerResult.newResult()
//        .error(graphqlError)
//        .build()
//      CompletableFuture.completedFuture(result)
//    } else {
//      super@DataFetcherExceptionHandler.handleException(handlerParameters)
//    }
//  }
}
