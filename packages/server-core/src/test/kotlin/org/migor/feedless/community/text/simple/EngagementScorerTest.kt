package org.migor.feedless.community.text.simple

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.migor.feedless.community.TokenizerService
import org.migor.feedless.repository.any
import org.migor.feedless.repository.any2
import org.mockito.Mockito
import org.mockito.Mockito.mock

@Tag("nlp")
class EngagementScorerTest {

  private lateinit var scorer: EngagementScorer
  private lateinit var commentGraphService: CommentGraphService

  @BeforeEach
  fun setUp() {
    commentGraphService = mock(CommentGraphService::class.java)

    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    scorer = EngagementScorer(commentGraphService)
  }

  @ParameterizedTest
  @CsvSource(
    value = [
      "0, 0.0",
      "5, 1.0",
      "10, 0.5",
      "15, 0.08",
      "20, 0.0",
    ]
  )
  fun scoreEngagement(repliesCount: Int, expected: Double) = runTest {
    val comment = mock(CommentEntity::class.java)
    Mockito.`when`(commentGraphService.getReplyCount(any2())).thenReturn(repliesCount)
    assertThat(scorer.score(comment)).isCloseTo(expected, within(0.01))
  }
}
