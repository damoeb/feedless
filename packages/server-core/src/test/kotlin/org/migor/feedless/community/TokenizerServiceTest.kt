package org.migor.feedless.community

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.util.*

@Tag("nlp")
class TokenizerServiceTest {

  private lateinit var service: TokenizerService

  @BeforeEach
  fun setUp() {
    service = TokenizerService()
    service.postConstruct()
  }

  @Test
  fun `tokenize german text works`() {
    assertThat(service.tokenizeWords("Hallo mein Name ist foo", Locale.GERMAN)).isEqualTo(
      arrayOf(
        "Hallo",
        "mein",
        "Name",
        "ist",
        "foo"
      )
    )
  }

  @Test
  fun `tokenize english text works`() {
    assertThat(service.tokenizeWords("Hello my name is foo", Locale.ENGLISH)).isEqualTo(
      arrayOf(
        "Hello",
        "my",
        "name",
        "is",
        "foo"
      )
    )
  }

  @Test
  fun `tokenize as words works`() {
    assertThat(
      service.tokenizeWords(
        "Hello, can you help me accessing the website https://example.org",
        Locale.ENGLISH
      )
    ).isEqualTo(arrayOf("Hello", "can", "you", "help", "me", "accessing", "the", "website", "https://example.org"))
  }
}
