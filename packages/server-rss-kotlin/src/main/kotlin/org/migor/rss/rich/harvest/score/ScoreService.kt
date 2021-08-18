package org.migor.rss.rich.harvest.score

import org.jsoup.Jsoup
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.service.FeedService.Companion.absUrl
import org.springframework.stereotype.Service

data class StaticArticleScores(var wordCount: Int? = null, var paragraphCount: Int? = null, var outgoingLinksCount: Int? = null)
data class DynamicArticleScores(var incomingLinkCount: Int? = null)
data class ArticleScores(
  var stat: StaticArticleScores = StaticArticleScores(),
  var dyn: DynamicArticleScores = DynamicArticleScores())

@Service
class ScoreService {

  private val word: Regex = Regex("\\w")

  fun scoreStatic(article: Article): Article {
    if (article.hasReadability == true) {
      val readability = article.readability
      val doc = Jsoup.parse(readability!!.content)
      val outgoingLinksCount = doc.body().select("a[href]")
        .map { link -> absUrl(article.url!!, link.attr("href")) }
        .distinct()
        .count()

      val scores = ArticleScores()
      scores.stat.outgoingLinksCount = outgoingLinksCount
      scores.stat.wordCount = readability.textContent!!.split(" ").filter { w -> w.matches(word) }.count()
      scores.stat.paragraphCount = doc.body().select("p").filter { n -> n.hasText() }.count()
      article.scores = scores
    }
    return article
  }

  fun scoreDynamic(article: Article): Article {
    return article
  }

}
