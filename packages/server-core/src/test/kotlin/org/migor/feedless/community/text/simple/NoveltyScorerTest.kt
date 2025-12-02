package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.mockito.Mockito
import org.mockito.Mockito.mock

class NoveltyScorerTest {

  private lateinit var scorer: NoveltyScorer

  @BeforeEach
  fun setUp() {
    scorer = NoveltyScorer()
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "0, 0.0",
      "2, 0.8",
      "3, 1.0",
      "4, 1.0",
    ]
  )
  fun score(input: Int, expected: Double) {
    assertThat(scorer.score(withUrls(input))).isCloseTo(expected, within(0.1))
  }
}


fun withUrls(urlCount: Int): CommentEntity {
  val words = mutableListOf<String>()
  for (i in 1..urlCount) {
    words.add("https://foo.bar")
  }
  val comment = mock(CommentEntity::class.java)
  Mockito.`when`(comment.text).thenReturn(words.joinToString(" "))
  return comment
}
