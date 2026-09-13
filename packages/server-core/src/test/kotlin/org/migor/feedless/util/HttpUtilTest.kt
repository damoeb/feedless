package org.migor.feedless.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.api.ApiParams
import org.springframework.mock.web.MockHttpServletRequest

class HttpUtilTest {

  @Test
  fun `keeps a valid client x-corr-id`() {
    val request = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, "req-1.trace:child/9_-") }

    assertThat(HttpUtil.corrIdOf(request)).isEqualTo("req-1.trace:child/9_-")
  }

  @Test
  fun `replaces a client x-corr-id longer than 64 characters`() {
    val request = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, "a".repeat(65)) }

    assertThat(HttpUtil.corrIdOf(request)).isNotEqualTo("a".repeat(65)).isNotBlank()
  }

  @Test
  fun `keeps a client x-corr-id of exactly 64 characters`() {
    val id = "a".repeat(64)
    val request = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, id) }

    assertThat(HttpUtil.corrIdOf(request)).isEqualTo(id)
  }

  @Test
  fun `replaces a client x-corr-id with a space`() {
    val request = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, "abc def") }

    assertThat(HttpUtil.corrIdOf(request)).isNotEqualTo("abc def").isNotBlank()
  }

  @Test
  fun `replaces a client x-corr-id with U+0085`() {
    val request = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, "abcdef") }

    assertThat(HttpUtil.corrIdOf(request)).isNotEqualTo("abcdef").isNotBlank()
  }

  @Test
  fun `replaces a client x-corr-id with CR or LF`() {
    val cr = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, "abc\rdef") }
    val lf = MockHttpServletRequest().apply { addHeader(ApiParams.corrId, "abc\ndef") }

    assertThat(HttpUtil.corrIdOf(cr)).isNotEqualTo("abc\rdef").isNotBlank()
    assertThat(HttpUtil.corrIdOf(lf)).isNotEqualTo("abc\ndef").isNotBlank()
  }

  @Test
  fun `generates one when none is sent`() {
    val request = MockHttpServletRequest()

    assertThat(HttpUtil.corrIdOf(request)).isNotBlank()
  }
}
