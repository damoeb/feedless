package org.migor.feedless.api.graphql

import com.netflix.graphql.types.errors.ErrorType
import graphql.Scalars
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.ExecutionStepInfo
import graphql.execution.ResultPath
import graphql.schema.DataFetchingEnvironmentImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.NotFoundException
import java.io.IOException

class GraphQLExceptionHandlerTest {

  private val handler = GraphQLExceptionHandler()

  @Test
  fun `a checked exception keeps its message`() {
    val result = handler.handleException(parametersFor(IOException("connection refused")))

    assertThat(result).isNotNull
    val error = result.get().errors.single()
    assertThat(error.message).isEqualTo("connection refused")
    assertThat(error.errorType).isEqualTo(ErrorType.UNKNOWN)
    assertThat(error.path).containsExactly("repository")
  }

  @Test
  fun `a domain exception keeps its mapped error type`() {
    val result = handler.handleException(parametersFor(NotFoundException("repository not found")))

    val error = result.get().errors.single()
    assertThat(error.message).isEqualTo("repository not found")
    assertThat(error.errorType).isEqualTo(ErrorType.NOT_FOUND)
  }

  private fun parametersFor(exception: Throwable): DataFetcherExceptionHandlerParameters {
    val stepInfo = ExecutionStepInfo.newExecutionStepInfo()
      .type(Scalars.GraphQLString)
      .path(ResultPath.rootPath().segment("repository"))
      .build()
    val env = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().executionStepInfo(stepInfo).build()
    return DataFetcherExceptionHandlerParameters.newExceptionParameters()
      .dataFetchingEnvironment(env)
      .exception(exception)
      .build()
  }
}
