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
import org.migor.rich.rss.harvest.BlacklistedForSiteHarvestException
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
@Profile("database")
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
        saveFulltext(corrId, article, fromMarkup(corrId, article.url!!, response.data))
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

      saveFulltext(corrId, article, harvest(corrId, url, askPrerender))

    }.onFailure {
      when (it) {
        is BlacklistedForSiteHarvestException -> {
          siteHarvestDAO.deleteById(siteHarvest.id)
        }
        is SiteNotFoundException -> {
          log.info("[$corrId] site not found, deleting article")
          // todo mag fix this
          siteHarvestDAO.deleteById(siteHarvest.id)
//          articleDao.deleteById(siteHarvest.articleId!!)
        }
        is HostOverloadingException -> {
          log.info("[$corrId] postpone harvest")
          siteHarvestDAO.delayHarvest(siteHarvest.id, Date(), datePlus(Duration.ofMinutes(3)))
        }
        else -> {
          log.warn("[${corrId}] Failed to extract: ${it.message}")
          siteHarvestDAO.persistErrorByArticleId(
            siteHarvest.articleId!!,
            it.message,
            Date(),
            null
          )
        }
      }
    }
  }
  @Transactional
  fun harvest(
    corrId: String,
    url: String,
    askPrerender: Boolean
  ): ExtractedArticle? {
    if (isBlacklistedForHarvest(url)) {
      log.warn("[$corrId] Blacklisted for harvesting $url")
      throw BlacklistedForSiteHarvestException(url)
    }

    val contentType = httpService.getContentTypeForUrl(corrId, url)
    val canPrerender = contentType == "text/html"
    if (!canPrerender && askPrerender) {
      log.warn("[$corrId] Overriding prerender-request, cause contentType=$contentType")
    }

    return if (canPrerender && askPrerender) {
      log.info("[$corrId] trigger prerendering for $url")
      val askPrerendering = MqAskPrerendering.Builder()
        .setUrl(url)
        .setCorrelationId(corrId)
        .build()
      rabbitTemplate.convertAndSend(RabbitQueue.askPrerendering, JsonUtil.gson.toJson(askPrerendering))
      return null
    } else {
      log.info("[$corrId] extracting from static content for $url")
      val response = httpService.httpGet(corrId, url, 200)
      extractFromAny(corrId, url, contentType, response)
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
    url: String,
    contentType: String?,
    response: HttpResponse
  ): ExtractedArticle? {
    log.warn("[${corrId}] mime $contentType")
    return when (contentType) {
      "text/html" -> fromMarkup(corrId, url, String(response.responseBody))
      "text/plain" -> fromText(corrId, url, response)
      "application/pdf" -> fromPdf(corrId, url, response)
      else -> {
        log.warn("[${corrId}] Cannot extract article from mime $contentType")
        null
      }
    }
  }

  private fun fromText(corrId: String, url: String, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from text")
    val extractedArticle = ExtractedArticle(url)
    extractedArticle.contentText = StringUtils.trimToNull(String(response.responseBody))
    return extractedArticle
  }

  fun fromPdf(corrId: String, url: String, response: HttpResponse): ExtractedArticle {
    log.info("[${corrId}] from pdf")
    ByteArrayInputStream(response.responseBody).use {
      val handler = BodyContentHandler()
      val metadata = Metadata()
      val parser = AutoDetectParser()
      val parseContext = ParseContext()
      parser.parse(it, handler, metadata, parseContext)

      val extractedArticle = ExtractedArticle(url)
      extractedArticle.title = metadata.get(TikaCoreProperties.TITLE)
      extractedArticle.contentText = handler.toString().replace("\n|\r|\t", " ")
      log.info("[${corrId}] pdf-content ${extractedArticle.contentText}")
      return extractedArticle
    }
  }

  private fun fromMarkup(corrId: String, url: String, markup: String): ExtractedArticle? {
    log.info("[${corrId}] from markup")
    return webToArticleTransformer.fromHtml(markup, url)
  }

  private fun saveFulltext(corrId: String, article: ArticleEntity, extractedArticle: ExtractedArticle?) {
    if (Optional.ofNullable(extractedArticle).isPresent) {
      val fulltext = extractedArticle!!
      log.info("[$corrId] fulltext present")
      var hasContent = false
      fulltext.title?.let {
        log.debug("[$corrId] title ${article.title} -> $it")
        article.title = it
      }
      fulltext.content?.let {
        log.debug(
          "[$corrId] contentRawMime ${article.contentRawMime} -> ${
            StringUtils.substring(
              fulltext.contentMime,
              0,
              100
            )
          }"
        )
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
      articleDao.saveFulltextContent(
        article.id,
        article.title,
        article.contentRaw,
        article.contentRawMime,
        article.contentSource,
        article.contentText,
        hasContent,
        article.imageUrl,
        Date()
      )
      siteHarvestDAO.deleteByArticleId(article.id)
    } else {
      siteHarvestDAO.persistErrorByArticleId(article.id, "null", Date(), datePlus(Duration.ofDays(1)))
      log.warn("[$corrId] failed readability for ${article.url}")
    }
  }
}
