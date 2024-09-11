package org.migor.feedless.community.text.simple

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.CommentGraphService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.community} & ${AppLayer.service}")
class CitationScorer {

  private val log = LoggerFactory.getLogger(KeywordIntersectionScorer::class.simpleName)

  @Autowired
  lateinit var commentGraphService: CommentGraphService

  private val quotePattern = Regex("\"([^\"]+)\"")

  suspend fun score(comment: CommentEntity): Double {
    val blockCitations = comment.contentText.split("\n").filter { it.startsWith(">") }
      .map { it.replace(Regex("^[> ]+"), "") }
    val inlineCitations = getInlineCitations(comment)

    return commentGraphService.getParent(comment)?.let {
      val parentText = it.contentText
      if (blockCitations.plus(inlineCitations).any { quote -> parentText.contains(quote) }) {
        1.0
      } else {
        0.0
      }
    } ?: 0.0
  }

  private fun getInlineCitations(comment: CommentEntity): List<String> {
    val text = comment.contentText

    return quotePattern.findAll(text).map { it.value }.toList()
  }

}
