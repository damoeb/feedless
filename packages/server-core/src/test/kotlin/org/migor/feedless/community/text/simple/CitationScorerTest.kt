package org.migor.feedless.community.text.simple

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.migor.feedless.community.TokenizerService
import org.migor.feedless.source.any
import org.mockito.Mockito
import org.mockito.Mockito.mock

class CitationScorerTest {

  private lateinit var scorer: CitationScorer
  private lateinit var commentGraphService: CommentGraphService

  @BeforeEach
  fun setUp() {
    commentGraphService = mock(CommentGraphService::class.java)

    val tokenizerService = TokenizerService()
    tokenizerService.postConstruct()

    scorer = CitationScorer()
    scorer.commentGraphService = commentGraphService

    val parent = mock(CommentEntity::class.java)
    Mockito.`when`(parent.content).thenReturn("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.")
    Mockito.`when`(commentGraphService.getParent(any(CommentEntity::class.java))).thenReturn(parent)
  }

  @Test
  fun score_block_quote() {
    val comment = mock(CommentEntity::class.java)
    Mockito.`when`(comment.content).thenReturn("""
> Ut enim ad minim veniam, quis nostrud exercitation
Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
    """.trimIndent())

    assertThat(scorer.score(comment)).isEqualTo(1.0)
  }

  @Test
  fun score_inline_quote() {
    val comment = mock(CommentEntity::class.java)
    Mockito.`when`(comment.content).thenReturn("""
Duis aute irure dolor "quis nostrud exercitation" in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
    """.trimIndent())

    assertThat(scorer.score(comment)).isEqualTo(1.0)
  }
}
