package org.migor.feedless.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FeedctlBaseUrlValidatorTest {

  @ParameterizedTest
  @ValueSource(
    strings = [
      "https://feedless.example.org",
      "http://localhost:8080",
      "https://feedless.example.org/api",
    ]
  )
  fun `whenUrlIsWellFormed_ThenValid`(url: String) {
    assertThat(FeedctlBaseUrlValidator.isValid(url)).isTrue()
  }

  @ParameterizedTest
  @ValueSource(
    strings = [
      "https://feedless.example.org\"; rm -rf ~ #",
      "https://feedless.example.org`id`",
      "https://feedless.example.org" + "\$(id)",
      "https://feedless.example.org with space",
      "https://feedless.example.org;id",
      "https://feedless.example.org?q=1",
      "https://feedless.example.org#f",
      "https://user@feedless.example.org",
      "ftp://feedless.example.org",
    ]
  )
  fun `whenUrlIsUnsafeOrNotHttp_ThenInvalid`(url: String) {
    assertThat(FeedctlBaseUrlValidator.isValid(url)).isFalse()
  }
}
