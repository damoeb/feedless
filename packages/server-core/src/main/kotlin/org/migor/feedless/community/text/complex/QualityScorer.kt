package org.migor.feedless.community.text.complex

import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.LanguageService
import org.migor.feedless.community.text.simple.CitationScorer
import org.migor.feedless.community.text.simple.EngagementScorer
import org.migor.feedless.community.text.simple.ReadingEaseScorer
import org.migor.feedless.community.text.simple.SpellingScorer
import org.migor.feedless.community.text.simple.VocabularyScorer
import org.migor.feedless.community.text.simple.WordCountScorer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

data class QualityWeights(
  val engagement: Double,
  val citation: Double,
  val vocabulary: Double,
  val spelling: Double,
  val wordCount: Double,
  val ease: Double,
)

@Service
@Profile(AppProfiles.community)
class QualityScorer {

  private val log = LoggerFactory.getLogger(QualityScorer::class.simpleName)

  @Autowired
  private lateinit var vocabularyScorer: VocabularyScorer

  @Autowired
  private lateinit var readingEaseScorer: ReadingEaseScorer

  @Autowired
  private lateinit var engagementScorer: EngagementScorer

  @Autowired
  private lateinit var wordCountScorer: WordCountScorer

  @Autowired
  private lateinit var citationScorer: CitationScorer

  @Autowired
  private lateinit var spellingScorer: SpellingScorer

  @Autowired
  private lateinit var languageService: LanguageService

  suspend fun quality(comment: CommentEntity, w: QualityWeights): Double {
    /*
    Length: Let L be the length of the post or comment (e.g., number of characters or words).
Engagement: Let E be a measure of engagement, such as the number of upvotes, replies, or shares.
References and Citations: Let R be the number of references or citations included in the post or comment.
Grammar and Spelling: Let G be a measure of grammar and spelling correctness (e.g., a score based on natural language processing analysis).

Q= wL*L + wE*E + wR*R + wG*G
     */

    val text = comment.contentText
    val locale = languageService.bestLocale(text)
    return arrayOf(
      w.wordCount * wordCountScorer.score(text, locale),
      w.engagement * engagementScorer.score(comment),
      w.citation * citationScorer.score(comment),

      // grammer
      w.ease * readingEaseScorer.score(text, locale),
      w.vocabulary * vocabularyScorer.score(text, locale),
      w.spelling * spellingScorer.calculateErrorRate(text, locale)
    ).average()
  }
}
