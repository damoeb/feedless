package org.migor.feedless.community.text.simple

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.migor.feedless.data.jpa.comment.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.migor.feedless.any2
import org.mockito.Mockito
import org.mockito.Mockito.mock

@Tag("nlp")
class CitationScorerTest {

  private lateinit var scorer: CitationScorer
  private lateinit var commentGraphService: CommentGraphService

  @BeforeEach
  fun setUp() {
    runBlocking {

      commentGraphService = mock(CommentGraphService::class.java)

      scorer = CitationScorer(commentGraphService)

      val parent = mock(CommentEntity::class.java)
      Mockito.`when`(parent.text)
        .thenReturn("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.")
      Mockito.`when`(commentGraphService.getParent(any2())).thenReturn(parent)
    }
  }

  @Test
  fun score_block_quote() = runTest {

    val comment = mock(CommentEntity::class.java)
    Mockito.`when`(comment.text).thenReturn(
      """
> Ut enim ad minim veniam, quis nostrud exercitation
Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
    """.trimIndent()
    )

    assertThat(scorer.score(comment)).isEqualTo(1.0)
  }

  @Test
  fun score_inline_quote() = runTest {
    val comment = mock(CommentEntity::class.java)
    Mockito.`when`(comment.text).thenReturn(
      """
Duis aute irure dolor "quis nostrud exercitation" in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
    """.trimIndent()
    )

    assertThat(scorer.score(comment)).isEqualTo(1.0)
  }
}
