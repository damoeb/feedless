package org.migor.feedless.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class HostsTest {

  @ParameterizedTest
  @CsvSource(
    "https://www.Bueron.ch/index.php?apid=1, www.bueron.ch",
    "http://example.org:8080/a, example.org",
    "https://user:pw@example.org/a, example.org",
    "example.org/path, example.org",
    "HTTPS://EXAMPLE.ORG, example.org",
  )
  fun `hostOf lower-cases the host and ignores scheme, port and user info`(url: String, expected: String) {
    assertThat(hostOf(url)).isEqualTo(expected)
  }

  @ParameterizedTest
  @CsvSource("''", "'://'", "'http://'")
  fun `hostOf is null without a host`(url: String) {
    assertThat(hostOf(url)).isNull()
  }
}
