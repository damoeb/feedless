package org.migor.feedless.common

import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
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
    httpService = HttpService(testPublicUrls(apiGatewayUrl = "http://localhost"), HostCooldownGuard(cooldowns))
    httpService.postConstruct()
  }

  @AfterEach
  fun tearDown() {
    server.stop(0)
  }

  private fun url() = "http://localhost:${server.address.port}/feed"

  @Test
  fun `a host allows a burst of 5, then about 1 request per second`() = runTest {
    val bucket = httpService.resolveHostBucket(java.net.URI("https://www.newsweek.com/a").toURL())

    repeat(5) { assertThat(bucket.tryConsume(1)).isTrue() }
    val sixth = bucket.tryConsumeAndReturnRemaining(1)

    assertThat(sixth.isConsumed).isFalse()
    assertThat(Duration.ofNanos(sixth.nanosToWaitForRefill)).isLessThanOrEqualTo(Duration.ofSeconds(1))
  }

  @Test
  fun `the same url is fetched at most twice a minute`() = runTest {
    val bucket = httpService.resolveUrlBucket(java.net.URI("https://www.newsweek.com/a").toURL())

    repeat(2) { assertThat(bucket.tryConsume(1)).isTrue() }

    assertThat(bucket.tryConsume(1)).isFalse()
  }

  // localhost is the gateway, which is never throttled.
  private fun throttledUrl(path: String) = "http://127.0.0.1:${server.address.port}/$path"

  @Test
  fun `given a caller that handles backpressure, an empty host bucket fails fast`() = runTest {
    withContext(Backpressure) {
      repeat(HttpService.HOST_BURST.toInt()) { httpService.httpGet(throttledUrl("a$it"), 200) }

      val e = runCatching { httpService.httpGet(throttledUrl("next"), 200) }.exceptionOrNull()

      assertThat(e).isInstanceOf(HostOverloadingException::class.java)
    }
  }

  @Test
  fun `given a caller without backpressure, an empty host bucket waits`() = runTest {
    repeat(HttpService.HOST_BURST.toInt()) { httpService.httpGet(throttledUrl("a$it"), 200) }

    val response = httpService.httpGet(throttledUrl("next"), 200)

    assertThat(response.statusCode).isEqualTo(200)
  }

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
  fun `a redirect loop is a strike on the host`() = runTest {
    server.createContext("/loop") { exchange ->
      exchange.responseHeaders.add("Location", "/loop")
      exchange.sendResponseHeaders(302, -1)
      exchange.close()
    }

    val e = runCatching { httpService.httpGet("http://localhost:${server.address.port}/loop", 200) }.exceptionOrNull()

    assertThat(e).isInstanceOf(HostBlockedException::class.java)
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

  @Test
  fun `a 429 after a redirect to another host is recorded under the request host`() = runTest {
    val port = server.address.port
    server.createContext("/a") { exchange ->
      exchange.responseHeaders.add("Location", "http://127.0.0.1:$port/b")
      exchange.sendResponseHeaders(302, -1)
      exchange.close()
    }
    server.createContext("/b") { exchange ->
      exchange.sendResponseHeaders(429, -1)
      exchange.close()
    }

    val e = runCatching { httpService.httpGet("http://localhost:$port/a", 200) }.exceptionOrNull()

    assertThat(e).isInstanceOf(HostOverloadingException::class.java)
    assertThat(cooldowns.rows).containsKey("localhost")
    assertThat(cooldowns.rows).doesNotContainKey("127.0.0.1")
  }
}
