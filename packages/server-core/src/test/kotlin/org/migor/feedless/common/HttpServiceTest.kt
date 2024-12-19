package org.migor.feedless.common

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.MalformedURLException

class HttpServiceTest {

  private lateinit var httpService: HttpService

  @BeforeEach
  fun setUp() {
    httpService = HttpService("https://localhost")
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
