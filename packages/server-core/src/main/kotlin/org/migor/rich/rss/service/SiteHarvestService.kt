package org.migor.rich.rss.service

import org.apache.commons.lang3.StringUtils
import org.apache.tika.metadata.Metadata
import org.apache.tika.metadata.TikaCoreProperties
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.sax.BodyContentHandler
import org.migor.rich.rss.api.HostOverloadingException
import org.migor.rich.rss.config.RabbitQueue
import org.migor.rich.rss.database.enums.ArticleSource
import org.migor.rich.rss.database.models.ArticleEntity
import org.migor.rich.rss.database.models.SiteHarvestEntity
import org.migor.rich.rss.database.repositories.ArticleDAO
import org.migor.rich.rss.database.repositories.SiteHarvestDAO
import org.migor.rich.rss.generated.MqAskPrerendering
import org.migor.rich.rss.generated.MqPrerenderingResponse
import org.migor.rich.rss.harvest.SiteNotFoundException
import org.migor.rich.rss.transform.ExtractedArticle
import org.migor.rich.rss.transform.WebToArticleTransformer
import org.migor.rich.rss.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayInputStream
import java.time.Duration
import java.util.*


@Service
@Profile("database2")
class SiteHarvestService {
  private val log = LoggerFactory.getLogger(SiteHarvestService::class.simpleName)

  @Autowired
  lateinit var httpService: HttpService

  @Autowired
  lateinit var webToArticleTransformer: WebToArticleTransformer

  @Autowired
  lateinit var rabbitTemplate: RabbitTemplate

  @Autowired
  lateinit var articleDao: ArticleDAO

  @Autowired
  lateinit var siteHarvestDAO: SiteHarvestDAO

  @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
  @RabbitListener(queues = [RabbitQueue.prerenderingResult])
  fun listenPrerenderResponse(prerenderResponseJson: String) {
    runCatching {
      val response = JsonUtil.gson.fromJson(prerenderResponseJson, MqPrerenderingResponse::class.java)
      val corrId = response.correlationId
      val article = Optional.ofNullable(articleDao.findByUrl(response.url!!))
        .orElseThrow { throw IllegalArgumentException("Article ${response?.url} not found") }

      if (response.error) {
        log.error("[${corrId}] Failed to prerender ${response.url}")
        saveFulltext(corrId, article, null)
      } else {
        saveFulltext(corrId, article, fromMarkup(corrId, article, response.data))
      }
    }.onFailure {
      this.log.error("Cannot handle readability ${it.message}")
    }
  }

  @Transactional
  fun harvest(
    corrId: String,
    siteHarvest: SiteHarvestEntity
  ) {
    val article = siteHarvest.article!!
    val feed = siteHarvest.feed!!

    runCatching {
      val askPrerender = feed.harvestSiteWithPrerender

      val url = article.url!!
      if(isBlacklistedForHarvest(url)) {
        log.warn("[$corrId] Blacklisted for harvesting $url")
        return
      }

      val contentType = httpService.getContentTypeForUrl(corrId, url)
      val canPrerender = contentType == "text/html"
      if (!canPrerender && askPrerender) {
        log.warn("[$corrId] Overriding prerender-request, cause contentType=$contentType")
      }

      if (canPrerender && askPrerender) {
        log.info("[$corrId] trigger prerendering for ${article.url}")
        val askPrerendering = MqAskPrerendering.Builder()
          .setUrl(article.url)
          .setCorrelationId(corrId)
          .build()
        rabbitTemplate.convertAndSend(RabbitQueue.askPrerendering, JsonUtil.gson.toJson(askPrerendering))
      } else {
        log.info("[$corrId] extracting from static content for ${article.url}")
        val response = httpService.httpGet(corrId, url, 200)
        saveFulltext(corrId, article, extractFromAny(corrId, article, contentType, response))
      }
      siteHarvestDAO.deleteById(siteHarvest.id)

    }.onFailure {
      when (it) {
        is HostOverloadingException -> {
          siteHarvestDAO.delayHarvest(siteHarvest.id, Date(), datePlus(Duration.ofMinutes(3)))
        }
        is SiteNotFoundException -> {
          log.info("[$corrId] site not found")
          siteHarvestDAO.deleteById(siteHarvest.id)
          articleDao.deleteById(siteHarvest.articleId!!)
        }
        else -> {
          log.error("[${corrId}] Failed to extract: ${it.message}")
          siteHarvestDAO.persistError(siteHarvest.id, siteHarvest.errorCount + 1, it.message, Date(), datePlus(Duration.ofHours(8)))
        }
      }
    }
  }

  private fun datePlus(duration: Duration): Date {
    return Date(System.currentTimeMillis() + duration.toMillis())
  }

  companion object {
    fun isBlacklistedForHarvest(url: String): Boolean {
      return listOf("https://twitter.com", "https://www.youtube.com", "https://youtub.be").any { url.startsWith(it) }
    }
  }

  private fun extractFromAny(
    corrId: String,
    article: ArticleEntity,
    contentType: String?,
    response: HttpResponse
  ): ExtractedArticle? {
    log.warn("[${corrId}] mime $contentType")
    return when (contentType) {
      "text/html" -> fromMarkup(corrId, article, String(response.responseBody))
      "text/plain" -> fromText(corrId, article, response)
      "application/pdf" -> fromPdf(corrId, article, response)
      else -> {
        log.warn("[${corrId}] Cannot extract article from mime $contentType")
        null
      }
    }
  }

  private fun fromText(corrId: String, article: ArticleEntity, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from text")
    val extractedArticle = ExtractedArticle(article.url!!)
    extractedArticle.contentText = StringUtils.trimToNull(String(response.responseBody))
    return extractedArticle
  }

  fun fromPdf(corrId: String, article: ArticleEntity, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from pdf")
    ByteArrayInputStream(response.responseBody).use {
      val handler = BodyContentHandler()
      val metadata = Metadata()
      val parser = AutoDetectParser()
      val parseContext = ParseContext()
      parser.parse(it, handler, metadata, parseContext)

      val extractedArticle = ExtractedArticle(article.url!!)
      extractedArticle.title = metadata.get(TikaCoreProperties.TITLE)
      extractedArticle.contentText = handler.toString().replace("\n|\r|\t", " ")
      log.info("[${corrId}] pdf-content ${extractedArticle.contentText}")
      return extractedArticle
    }
  }

  private fun fromMarkup(corrId: String, article: ArticleEntity, markup: String): ExtractedArticle? {
    log.info("[${corrId}] from markup")
    return webToArticleTransformer.fromHtml(markup, article.url!!)
  }

  private fun saveFulltext(corrId: String, article: ArticleEntity, extractedArticle: ExtractedArticle?): ArticleEntity {
    if (Optional.ofNullable(extractedArticle).isPresent) {
      val fulltext = extractedArticle!!
      log.info("[$corrId] fulltext present")
      var hasContent = false
      fulltext.title?.let {
        log.debug("[$corrId] title ${article.title} -> $it")
        article.title = it
      }
      fulltext.content?.let {
        log.debug("[$corrId] contentRawMime ${article.contentRawMime} -> ${StringUtils.substring(fulltext.contentMime, 0, 100)}")
        article.contentRaw = fulltext.content
        article.contentRawMime = fulltext.contentMime!!
        hasContent = true
      }
      log.debug("[$corrId] mainImageUrl ${article.imageUrl} -> ${StringUtils.substring(fulltext.imageUrl, 0, 100)}")
      article.imageUrl = StringUtils.trimToNull(fulltext.imageUrl)

      article.contentSource = ArticleSource.WEBSITE
      article.contentText = StringUtils.trimToEmpty(fulltext.contentText)

//      todo mag
//      val tags = Optional.ofNullable(article.tags).orElse(emptyList())
//        .toMutableSet()
//      tags.add(NamespacedTag(TagNamespace.CONTENT, "fulltext"))
//      article.tags = tags.toList()
      articleDao.saveFulltextContent(article.id, article.title, article.contentRaw, article.contentRawMime, article.contentSource, article.contentText, hasContent, article.imageUrl)
    } else {
      article.hasFulltext = false
      log.error("[$corrId] failed readability for ${article.url}")
    }
    return article
  }
}
