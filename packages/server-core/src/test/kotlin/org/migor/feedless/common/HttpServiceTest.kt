package org.migor.feedless.common

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.net.MalformedURLException

class HttpServiceTest {

  private lateinit var httpService: HttpService

  @BeforeEach
  fun setUp() {
    val propertyService = mock(PropertyService::class.java)
    `when`(propertyService.apiGatewayUrl).thenReturn("https://localhost")
    httpService = HttpService(propertyService)
    httpService.postConstruct()
  }

  @Test
  fun `httpGet will validate url`() {
    assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy {
      runTest {
        httpService.httpGet("gemma", 200)
      }
    }
  }
}
