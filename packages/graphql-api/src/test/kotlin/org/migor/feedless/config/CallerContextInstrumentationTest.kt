package org.migor.feedless.config

import graphql.ExecutionInput
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters
import graphql.schema.DataFetchingEnvironment
import graphql.schema.DataFetchingEnvironmentImpl
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.migor.feedless.capability.MdcKeys
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.session.LazyGrantedAuthority
import org.migor.feedless.user.UserId
import org.migor.feedless.util.JsonSerializer
import org.mockito.kotlin.mock
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.user.DefaultOAuth2User

class CallerContextInstrumentationTest {

  private val instrumentation = CallerContextInstrumentation()
  private val caller = UserId()

  @AfterEach
  fun tearDown() {
    SecurityContextHolder.clearContext()
    MDC.clear()
  }

  @Test
  fun `a field resolver on a thread without security context gets the caller captured on the request thread`() {
    loginAs(caller)
    MDC.put(MdcKeys.CORR_ID, "corr-1")
    val env = executionCapturedOnRequestThread()

    // what a pooled worker thread looks like
    SecurityContextHolder.clearContext()
    MDC.clear()
    val context = env.requestContext()

    assertThat(context.userId).isEqualTo(caller)
    assertThat(context.corrId).isEqualTo("corr-1")
  }

  @Test
  fun `an anonymous request is captured as anonymous even if a caller is left on the worker thread`() {
    val env = executionCapturedOnRequestThread()

    loginAs(UserId())
    val context = env.requestContext()

    assertThat(context.userId).isNull()
  }

  @Test
  fun `each field gets its own copy of the captured context`() {
    loginAs(caller)
    val env = executionCapturedOnRequestThread()

    assertThat(env.requestContext()).isNotSameAs(env.requestContext()).isEqualTo(env.requestContext())
  }

  @Test
  fun `without a captured caller the thread's security context decides`() {
    loginAs(caller)
    val env = DataFetchingEnvironmentImpl.newDataFetchingEnvironment().build()

    assertThat(env.requestContext().userId).isEqualTo(caller)
  }

  private fun executionCapturedOnRequestThread(): DataFetchingEnvironment {
    val input = ExecutionInput.newExecutionInput("{ __typename }").build()
    instrumentation.createState(InstrumentationCreateStateParameters(mock(), input))
    return DataFetchingEnvironmentImpl.newDataFetchingEnvironment().graphQLContext(input.graphQLContext).build()
  }

  private fun loginAs(userId: UserId) {
    val authorities = listOf(LazyGrantedAuthority(UserCapability.ID.value, JsonSerializer.toJson(userId)))
    val principal = DefaultOAuth2User(authorities, mapOf("id" to "test"), "id")
    SecurityContextHolder.getContext().authentication = OAuth2AuthenticationToken(principal, authorities, "test")
  }
}
