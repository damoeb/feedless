package org.migor.rich.rss.pipeline

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.migor.rich.rss.database.ArticleWithContext
import org.migor.rich.rss.database.enums.ArticleRefinementType
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.BucketEntity
import org.migor.rich.rss.database.models.RefinementEntity
import org.migor.rich.rss.service.ArticleService
import org.migor.rich.rss.service.FeedService.Companion.absUrl
import org.migor.rich.rss.service.GraphService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*

@Service
@Profile("database")
class FollowLinksHook {
  private val log = LoggerFactory.getLogger(FollowLinksHook::class.simpleName)

  @Autowired
  lateinit var articleService: ArticleService

  @Autowired
  lateinit var graphService: GraphService

  private fun followLinks(article: ArticleEntity, bucket: BucketEntity) {
    if (article.hasFulltext == true) {
      Optional.ofNullable(article.getContentOfMime("text/html"))
        .ifPresentOrElse({ content ->
          run {
            val doc = Jsoup.parse(content)
            val urls = doc.body().select("a[href]")
              .map { link -> absUrl(article.url!!, link.attr("href")) }
              .distinct()
              .filter { url -> StringUtils.isNotBlank(url) }
              .filter { url -> isQualifiedUrl(url) }

            urls.forEach { url -> graphService.link(article.url!!, url) }

            val groups = urls.groupBy { url -> URL(url).host }
            val firstUrlPerDomain = groups.keys.map { domain -> groups[domain]!!.first() }

            val toSeedFromUrls = Stack<String>()
            toSeedFromUrls.addAll(firstUrlPerDomain)
            toSeedFromUrls.shuffled()

            var seeded = 0
            while (seeded < 1 && toSeedFromUrls.isNotEmpty()) {
              val url = toSeedFromUrls.pop()
              val success = articleService.tryCreateArticleFromContainedUrlForBucket(url, article.url!!, bucket)
              if (success) {
                log.info("Seeded article from $url to bucket ${bucket.id}")
                seeded++
              }
            }
          }
        }, { log.error("Readability is null but hasReadability=true for ${article.url}") })
    }
  }

  private fun isQualifiedUrl(url: String): Boolean {
    val parsed = URL(url)
    if (StringUtils.isBlank(parsed.path.replace("/", ""))) {
      return false
    }

    return true
  }


}
