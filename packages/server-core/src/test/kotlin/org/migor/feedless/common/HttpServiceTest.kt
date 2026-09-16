package org.migor.feedless.common

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.HostBlockedException
import org.migor.feedless.HostOverloadingException
import org.migor.feedless.SiteNotFoundException
import org.migor.feedless.hostCooldown.HostCooldownState
import java.net.InetSocketAddress
import java.net.MalformedURLException
import java.time.Duration
import java.time.LocalDateTime

class HttpServiceTest {

  private lateinit var httpService: HttpService
  private lateinit var server: HttpServer
  private lateinit var cooldowns: FakeHostCooldown
  private var status = 200
  private var retryAfter: String? = null
  private var requests = 0

  @BeforeEach
  fun setUp() {
    server = HttpServer.create(InetSocketAddress("localhost", 0), 0)
    server.createContext("/") { exchange ->
      requests++
      // HttpResponse.contentType is non-null, so the success path needs a header to convert against.
      exchange.responseHeaders.add("Content-Type", "text/plain")
      retryAfter?.let { exchange.responseHeaders.add("Retry-After", it) }
      val body = "ok".toByteArray()
      exchange.sendResponseHeaders(status, body.size.toLong())
      exchange.responseBody.use { it.write(body) }
    }
    server.start()
    cooldowns = FakeHostCooldown()
    httpService = HttpService("http://localhost", HostCooldownGuard(cooldowns))
    httpService.postConstruct()
  }

  @AfterEach
  fun tearDown() {
    server.stop(0)
  }

  private fun url() = "http://localhost:${server.address.port}/feed"

  @Test
  fun `httpGet will validate url`() {
    assertThatExceptionOfType(MalformedURLException::class.java).isThrownBy {
      runTest { httpService.httpGet("gemma", 200) }
    }
  }

  @Test
  fun `429 with Retry-After records a throttle with the parsed delay`() = runTest {
    status = 429
    retryAfter = "600"

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()

    assertThat(e).isInstanceOf(HostOverloadingException::class.java)
    assertThat((e as HostOverloadingException).nextRetryAfter).isEqualTo(Duration.ofSeconds(600))
    assertThat(cooldowns.rows["localhost"]!!.lastStatus).isEqualTo(429)
  }

  @Test
  fun `429 without Retry-After falls back to 5 minutes`() = runTest {
    status = 429

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull() as HostOverloadingException

    assertThat(e.nextRetryAfter).isEqualTo(Duration.ofMinutes(5))
  }

  @Test
  fun `503 is a throttle`() = runTest {
    status = 503
    retryAfter = "120"

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull() as HostOverloadingException

    assertThat(e.nextRetryAfter).isEqualTo(Duration.ofSeconds(120))
  }

  @Test
  fun `403 and 401 are blocks`() = runTest {
    status = 403
    assertThat(runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()).isInstanceOf(HostBlockedException::class.java)
    assertThat(cooldowns.rows["localhost"]!!.strikes).isEqualTo(1)
  }

  @Test
  fun `404 stays not found`() = runTest {
    status = 404
    assertThat(runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()).isInstanceOf(SiteNotFoundException::class.java)
    assertThat(cooldowns.rows).isEmpty()
  }

  @Test
  fun `a cooling host is not requested`() = runTest {
    status = 429
    runCatching { httpService.httpGet(url(), 200) }
    val before = requests

    val e = runCatching { httpService.httpGet(url(), 200) }.exceptionOrNull()

    assertThat(e).isInstanceOf(HostOverloadingException::class.java)
    assertThat(requests).isEqualTo(before)
  }

  @Test
  fun `success resets a known cooldown`() = runTest {
    cooldowns.rows["localhost"] = HostCooldownState(
      "localhost", LocalDateTime.now().minusMinutes(1), 3, 403
    )

    httpService.httpGet(url(), 200)

    assertThat(cooldowns.rows).isEmpty()
  }
}
