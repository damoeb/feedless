package org.migor.feedless.api.graphql

import com.netflix.graphql.types.errors.ErrorType
import com.netflix.graphql.types.errors.TypedGraphQLError
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import org.migor.feedless.api.ApiParams
import org.migor.feedless.harvest.HostOverloadingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.concurrent.CompletableFuture


@Component
class CustomExceptionHandler : DataFetcherExceptionHandler {
  private val log = LoggerFactory.getLogger(CustomExceptionHandler::class.simpleName)

  override fun handleException(handlerParameters: DataFetcherExceptionHandlerParameters?): CompletableFuture<DataFetcherExceptionHandlerResult> {
    return when(handlerParameters?.exception) {
      is RuntimeException, is IllegalAccessException -> run {
        val corrId = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request.getHeader(ApiParams.corrId)
        log.warn("[$corrId] ${handlerParameters.exception.message}")
//        val debugInfo: MutableMap<String, Any> = HashMap()
//      debugInfo["somefield"] = "somevalue"
      val graphqlError: GraphQLError = TypedGraphQLError.newInternalErrorBuilder()
        .message("${handlerParameters.exception.message} (corrId: $corrId)")
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
