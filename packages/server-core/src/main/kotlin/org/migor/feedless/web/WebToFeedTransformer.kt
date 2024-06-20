package org.migor.feedless.web

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.apache.commons.lang3.BooleanUtils
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.WebToFeedParamsV2
import org.migor.feedless.api.dto.RichArticle
import org.migor.feedless.api.dto.RichFeed
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.DateClaimer
import org.migor.feedless.generated.types.ScrapePage
import org.migor.feedless.generated.types.ScrapeRequest
import org.migor.feedless.util.CryptUtil
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.stream.Collectors
import kotlin.math.ln


data class GeneralizedContext(
  val contextXPath: String,
  val extendContext: ExtendContext
)

data class XPathMatch(
  val p: String,
  val id: String? = null,
  val match: XPathNodeMatch? = null
)

data class XPathNodeMatch(
  val path: String,
  val index: String,
)

data class ContextVicinity(
  val context: Element,
  val previous: Element?,
  val next: Element?
) {
  operator fun get(propertyName: String): Element? {
    return when (propertyName) {
      "context" -> context
      "previous" -> previous
      "next" -> next
      else -> throw IllegalArgumentException("$propertyName not available")
    }
  }

  fun hasProperty(propertyName: String): Boolean {
    return try {
      Optional.ofNullable(get(propertyName)).map { true }.orElse(false)
    } catch (e: IllegalArgumentException) {
      false
    }

  }
}

data class LinkPointer(
  val element: Element,
//  val index: Int,
  val path: String
)

abstract class Selectors {
  abstract val linkXPath: String
  abstract val extendContext: ExtendContext
  abstract val contextXPath: String
  abstract val dateXPath: String?
  abstract val dateIsStartOfEvent: Boolean
}

data class GenericFeedRule(
  override val linkXPath: String,
  override val extendContext: ExtendContext,
  override val contextXPath: String,
  override val dateXPath: String?,
  override val dateIsStartOfEvent: Boolean = false,
  val feedUrl: String,
  val count: Int?,
  val score: Double,
  val samples: List<RichArticle> = emptyList()
) : Selectors()

@JsonIgnoreProperties
data class GenericFeedParserOptions(
  val strictMode: Boolean = false,
  val version: String = "0.1",
  val minLinkGroupSize: Int = 2,
  val minWordCountOfLink: Int = 1,
)

enum class PuppeteerWaitUntil {
  networkidle0,
  networkidle2,
  load,
  domcontentloaded

}

@JsonIgnoreProperties("recovery")
data class GenericFeedRefineOptions(
  val filter: String? = null,
)

@JsonIgnoreProperties
data class GenericFeedSelectors(
  val count: Int? = null,
  val score: Double? = null,
  val contexts: List<ArticleContext>? = null,
  override val linkXPath: String,
  override val extendContext: ExtendContext,
  override val contextXPath: String,
  override val dateXPath: String? = null,
  override val dateIsStartOfEvent: Boolean = false
) : Selectors()


data class ArticleContext(
  val linkElement: Element,
  var dateElement: Element?,
  val id: String,
  // root of article
  val contextElement: Element
)

enum class ExtendContext(val value: String) {
  PREVIOUS("p"),
  NEXT("n"),
  PREVIOUS_AND_NEXT("pn"),
  NONE(""),
}

/**
 * Issues:
 * - semantic tags are not valued as much as they should
 * - choosing context node may result in more rules
 */
@Service
class WebToFeedTransformer(

  @Autowired
  private var propertyService: PropertyService,
  @Autowired
  private var webToTextTransformer: WebToTextTransformer,
  @Autowired
  private var dateClaimer: DateClaimer
) {

  private val log = LoggerFactory.getLogger(WebToFeedTransformer::class.simpleName)

  private val reLinebreaks = Regex("^[\n\t\r ]+|[\n\t\r ]+$")
  private val reXpathId = Regex("(.*)\\[@id=(.*)\\]")
  private val reXpathIndexNode = Regex("([^\\[]+)\\[([0-9]+)\\]?")

  fun parseFeedRules(
    corrId: String,
    document: Document,
    url: URL,
    parserOptions: GenericFeedParserOptions,
    sampleSize: Int = 0
  ): List<GenericFeedRule> {
    val body = document.body()

    val linkElements: List<LinkPointer> = findLinks(document, parserOptions).distinctBy { it.element.attr("href") }

    // group links with similar path in document
    val linkGroups = groupLinksByPath(linkElements)

    log.debug("Found ${linkGroups.size} link groups")

    val scrapeRequest = ScrapeRequest.newBuilder()
      .page(
        ScrapePage.newBuilder()
          .url(url.toString())
          .build()
      )
      .build()

    val refineOptions = GenericFeedRefineOptions()

    return linkGroups
      .mapTo(mutableListOf()) { entry -> Pair(entry.key, entry.value) }
      .filterTo(ArrayList()) { (groupId, linksInGroup): Pair<String, MutableList<LinkPointer>> ->
        hasRelevantSize(
          groupId,
          linksInGroup,
          parserOptions
        )
      }
      .map { (groupId, linksInGroup) -> findArticleContext(groupId, linksInGroup) }
      .map { contexts -> tryAddDateXPath(contexts) }
      .map { contexts -> convertContextsToRule(contexts, body) }
      .map { selectors -> scoreRule(selectors) }
      .sortedByDescending { it.score }
      .map { selectors ->
        GenericFeedRule(
          feedUrl = createFeedUrl(url, selectors, parserOptions, scrapeRequest, refineOptions),
          count = selectors.count,
          score = selectors.score!!,
          linkXPath = selectors.linkXPath,
          extendContext = selectors.extendContext,
          contextXPath = selectors.contextXPath,
          dateXPath = selectors.dateXPath,
          samples = getArticlesBySelectors(corrId, selectors, document, url, sampleSize)
        )
      }
      .toList()
  }

//  private fun findPaginationXPath(
//    groupedLinks: HashMap<String, MutableList<LinkPointer>>,
//    url: String,
//    document: Document
//  ): String? {
//    return Optional.ofNullable(findPaginationElement(groupedLinks, url))
//      .map { "/" + this.getRelativeXPath(it, document.body()) }
//      .orElse(null)
//  }

//  private fun findPaginationElement(
//    groupedLinks: HashMap<String, MutableList<LinkPointer>>,
//    url: String
//  ): Element? {
//    val ed = org.apache.commons.text.similarity.LevenshteinDistance()
//    return groupedLinks
//      .values
//      .filter { it.size > 2 }
//      .map {
//        Pair(it, it.map {
//          ed.apply(absUrl(url, it.element.attr("href")), url)
//        }.average())
//      }
//      .sortedBy { listAndEditDistance -> listAndEditDistance.second }
//      .filter { it.second < 4 }
//      .map { it.first }
//      .map { linkPointers ->
//        this.findCommonParentElement("", linkPointers.map { it.element }).distinct()
//      }
//      .filter { it.size == 1 }
//      .map { it.first() }
//      .firstOrNull()
//  }

  private fun tryAddDateXPath(contexts: List<ArticleContext>): List<ArticleContext> {
    val hasTimeField = contexts.all { context ->
      Optional.ofNullable(context.contextElement.selectFirst("time")).map { true }.orElse(false)
    }

    return if (hasTimeField) {
      log.debug("has time field")
      contexts.map { context ->
        run {
          context.dateElement = context.contextElement.selectFirst("time")!!
          context
        }
      }
    } else {
      contexts
    }
  }

  private fun hasRelevantSize(
    groupId: String,
    linksInGroup: MutableList<LinkPointer>,
    parserOptions: GenericFeedParserOptions
  ): Boolean {
    val hasEnoughMembers = linksInGroup.size >= parserOptions.minLinkGroupSize

    if (hasEnoughMembers) {
      log.debug("Relevant: Yes (${linksInGroup.size} links) $groupId")
    } else {
      log.debug("Relevant: No (${linksInGroup.size} links) $groupId")
    }

    return hasEnoughMembers
  }

  private fun groupLinksByPath(linkElements: List<LinkPointer>) =
    linkElements.fold(HashMap<String, MutableList<LinkPointer>>()) { linkGroup, linkPath ->
      run {
        val groupId = linkPath.path
        if (!linkGroup.containsKey(groupId)) {
          linkGroup[groupId] = mutableListOf()
        }
        linkGroup[groupId]!!.add(linkPath)
        //        this.log.debug("group $groupId add ${linkPath.index}")
        linkGroup
      }
    }

  fun createFeedUrl(
    url: URL,
    selectors: Selectors,
    parserOptions: GenericFeedParserOptions,
    scrapeRequest: ScrapeRequest,
    refineOptions: GenericFeedRefineOptions
  ): String {
    val encode: (value: String) -> String = { value -> URLEncoder.encode(value, StandardCharsets.UTF_8) }
    val params: List<Pair<String, String>> = mapOf(
      WebToFeedParamsV2.version to propertyService.webToFeedVersion,
      WebToFeedParamsV2.url to url.toString(),
      WebToFeedParamsV2.linkPath to selectors.linkXPath,
      WebToFeedParamsV2.contextPath to selectors.contextXPath,
      WebToFeedParamsV2.datePath to StringUtils.trimToEmpty(selectors.dateXPath),
      WebToFeedParamsV2.extendContext to selectors.extendContext.value,
      WebToFeedParamsV2.prerender to "${scrapeRequest.page.prerender != null}",
      WebToFeedParamsV2.eventFeed to selectors.dateIsStartOfEvent,
      WebToFeedParamsV2.filter to StringUtils.trimToEmpty(refineOptions.filter),
    ).map { entry -> entry.key to encode("${entry.value}") }

    val searchParams = params.fold("") { acc, pair -> acc + "${pair.first}=${pair.second}&" }
    return "${propertyService.apiGatewayUrl}${ApiUrls.webToFeed}?$searchParams"
  }

  fun getFeedBySelectors(
    corrId: String,
    selectors: Selectors,
    document: Document,
    url: URL,
    sampleSize: Int = 0
  ): RichFeed {
    val richFeed = RichFeed()
    richFeed.id = url.toString()
    richFeed.title = document.title()
    richFeed.websiteUrl = url.toString()
    richFeed.publishedAt = Date()
    richFeed.items = getArticlesBySelectors(corrId, selectors, document, url)
    richFeed.feedUrl = ""
    return richFeed
  }

  fun getArticlesBySelectors(
    corrId: String,
    selectors: Selectors,
    document: Document,
    url: URL,
    sampleSize: Int = 0
  ): List<RichArticle> {

    val now = Date()
    val locale = extractLocale(document, propertyService.locale)
    log.debug("[${corrId}] getArticlesByRule context=${selectors.contextXPath} link=${selectors.linkXPath} date=${selectors.dateXPath}")
    val articles = evaluateXPath(selectors.contextXPath, document).mapNotNull { element ->
      try {
        val content = applyExtendElement(selectors.extendContext, normalizeTags(element.clone()))
        val link = resolveLink(url, selectors, element)
        link?.let {
          val date =
            Optional.ofNullable(StringUtils.trimToNull(selectors.dateXPath))
              .map { dateXPath ->
                run {
                  val pubDate = Optional.ofNullable(extractPubDate(corrId, dateXPath, element, locale))
                  evaluateXPath(dateXPath, element).first().remove()
                  pubDate.orElse(now)
                }
              }
              .orElse(now)

          val (pubDate, startingDate) = if (BooleanUtils.isTrue(selectors.dateIsStartOfEvent)) {
            if (now.before(date)) {
              Pair(now, date)
            } else {
              Pair(date, date)
            }
          } else {
            Pair(date, null)
          }
          val linkText = it.first
          val articleUrl = it.second

          val article = RichArticle()
          article.id = FeedUtil.toURI("article", articleUrl)
          article.title = StringUtils.substring(linkText.replace(reLinebreaks, " "), 0, 100)
          article.url = articleUrl
          article.contentText = webToTextTransformer.extractText(content)
          article.contentRawBase64 = withAbsUrls(content, url).selectFirst("body")!!.html()
          article.contentRawMime = "text/html"
          article.publishedAt = pubDate
          article.startingAt = startingDate

          if (qualifiesAsArticle(element, selectors)) {
            article
          } else {
            null
          }
        }

      } catch (e: Exception) {
        log.warn("[${corrId}] getArticlesByRule ${e.message}")
        null
      }
    }

    log.debug("[${corrId}] -> ${articles.size} articles")

    return articles.filterIndexed { index, _ -> sampleSize == 0 || index <= sampleSize }
  }

  private fun normalizeTags(element: Element): Element {
    element.select("table").tagName("div").attr("role", "table")
    element.select("thead").tagName("div").attr("role", "thead")
    element.select("tbody").tagName("div").attr("role", "tbody")
    element.select("tfoot").tagName("div").attr("role", "tfoot")
    element.select("tr").tagName("div").attr("role", "row")
    element.select("td").tagName("div").attr("role", "cell")
    element.select("font").tagName("div").attr("role", "font")
    element.select("form").tagName("div").attr("role", "form")
    return element
  }

  private fun resolveLink(
    url: URL,
    selectors: Selectors,
    element: Element
  ): Pair<String, String>? {
    return evaluateXPath(selectors.linkXPath, element)
      .map {
        Pair(
          it.text(), toAbsoluteUrl(
            url,
            StringUtils.trimToNull(it.attr("href"))
              ?: ("/hash/" + CryptUtil.sha1(StringUtils.deleteWhitespace(element.wholeText())))
          )
        )
      }
      .firstOrNull()
  }

  private fun extractLocale(document: Document, fallback: Locale): Locale {
    val langStr = document.select("html[@lang]").attr("lang")
    return Optional.ofNullable(StringUtils.trimToNull(langStr))
      .map {
        run {
          log.debug("Found lang ${it}")
          Locale.forLanguageTag(it)
        }
      }
      .orElse(fallback)
  }

  private fun extractPubDate(corrId: String, dateXPath: String, element: Element, locale: Locale): Date? {
    return runCatching {
      val timeElement = evaluateXPath(dateXPath, element).first()
      if (timeElement.hasAttr("datetime")) {
        dateClaimer.claimDatesFromString(corrId, timeElement.attr("datetime"), locale)
      } else {
        dateClaimer.claimDatesFromString(corrId, timeElement.text(), locale)
      }
    }.getOrNull()
  }

  private fun applyExtendElement(extendContext: ExtendContext, element: Element): Element {
    val p =
      if (arrayOf(
          ExtendContext.PREVIOUS,
          ExtendContext.PREVIOUS_AND_NEXT
        ).contains(extendContext)
      ) element.previousElementSibling()
        ?.outerHtml() else ""
    val n =
      if (arrayOf(
          ExtendContext.NEXT,
          ExtendContext.PREVIOUS_AND_NEXT
        ).contains(extendContext)
      ) element.nextElementSibling()?.outerHtml() else ""
    return parseHtml("<div>${StringUtils.trimToEmpty(p)}${element.outerHtml()}${StringUtils.trimToEmpty(n)}</div>", "")
  }


  private fun getRelativeCssPath(nodeParam: Element, context: Element, strictMode: Boolean): String {
    if (nodeParam == context) {
      // todo mag this is not applicable
      return "self"
    }
    var node = nodeParam
    var path = node.tagName() // tagName for text nodes is undefined
    while (node.parentNode() !== context && node.hasParent()) {
      node = node.parent()!!
      path = "${getNodeName(node, strictMode)}>${path}"
    }
    return path
  }

  private fun getNodeName(node: Element, strictMode: Boolean): String {
    return if (strictMode) {
      var childId = 0
      var ps = node.previousElementSibling()
      while (ps != null) {
        childId++
        ps = ps.previousElementSibling()
      }

      node.tagName() + childId
    } else {
      node.tagName()
    }
  }

  private fun toWords(text: String): List<String> {
    return text.trim().split(" ").filterTo(ArrayList()) { word: String -> word.isNotEmpty() }
  }

  private fun toAbsoluteUrl(url: URL, link: String): String {
    return URL(url, link).toString()
  }

  private fun qualifiesAsArticle(elem: Element, selectors: Selectors): Boolean {
    if (elem.text()
        .replace("\r", "")
        .replace("\n", "")
        .replace("\t", "")
        .isEmpty()
    ) {
      return false
    }
    val links = evaluateXPath(selectors.linkXPath, elem)
    return links.isNotEmpty()

  }

  fun __generalizeXPaths(xpaths: Collection<String>): String {
    val tokenized = xpaths.map { xpath ->
      xpath.split('/')
        .filterTo(ArrayList()) { xpathFragment: String -> xpathFragment.isNotEmpty() }
        .map { p ->
          run {
            val attrNodeMatch = reXpathId.matchEntire(p)
            val indexNodeMatch = reXpathIndexNode.matchEntire(p)
            if (indexNodeMatch != null) {
              XPathMatch(
                p,
                match = XPathNodeMatch(
                  path = indexNodeMatch.groupValues[1],
                  index = indexNodeMatch.groupValues[2]
                )
              )
            } else {
              if (attrNodeMatch != null) {
                XPathMatch(
                  p = attrNodeMatch.groupValues[1],
                  id = attrNodeMatch.groupValues[2]
                )
              } else {
                XPathMatch(
                  p = p
                )
              }
            }
          }
        }
    }
    val templateXPath = tokenized.first().mapIndexed { index, xPathSegment ->
      run {
        if (xPathSegment.id != null) {
          val allIds = tokenized.map { tokens -> tokens[index].id }
            .toSet()
            .toList()

          if (allIds.isEmpty() || allIds.size > 3) {
            "${xPathSegment.p}[@id]"
          } else {
            if (allIds.size == 1) {
              "${xPathSegment.p}[@id=${allIds[0]}]"
            } else {
              "${xPathSegment.p}[${allIds.joinToString(" or ") { id -> "contains(id, ${id})" }}]"
            }
          }
        } else if (xPathSegment.match != null) {
          val changingIndex = tokenized.stream()
            .map { otherXpathSegment -> otherXpathSegment[index].match!!.index }
            .collect(Collectors.toSet()).size > 1
          if (changingIndex) {
            xPathSegment.match.path
          } else {
            "${xPathSegment.match.path}[${xPathSegment.match.index}]"
          }
        } else {
          xPathSegment.p
        }
      }
    }

    return templateXPath.joinToString("/")
  }

  private fun getContextExtension(includeNextSibling: Boolean, includePreviousSibling: Boolean): ExtendContext {
    // node 0
    // prev 1
    // next 2
    // prev+next 3

    return if (includePreviousSibling || includeNextSibling) {
      if (includePreviousSibling) {
        ExtendContext.PREVIOUS
      } else {
        ExtendContext.NEXT
      }
    } else {
      ExtendContext.NONE
    }
  }

  private fun getRelativeXPath(element: Element, context: Element): String {

    if (element === context) {
      return ""
    }

    var ix = 0
    val siblings = element.parent()!!.children()

    for (sibling in siblings) {
      if (sibling === element) {
        return "${getRelativeXPath(element.parent()!!, context)}/${
          element.tagName().lowercase(Locale.getDefault())
        }[${ix + 1}]"
      }

      // todo sibling.nodeType === 1 &&
      if (sibling.tagName() === element.tagName()) {
        ix++
      }
    }
    throw IllegalArgumentException("Cannot generate xpath")
  }

  private fun evaluateXPath(xpath: String, context: Element): List<Element> {
    return if (xpath == "./") {
      listOf(context)
    } else {
      val xpathResult = Xsoup.compile(xpath.replaceFirst("./", "//")).evaluate(context).elements
      xpathResult.toList()
    }
  }

  private fun includeSibling(kind: String, vicinities: List<ContextVicinity>): Boolean {
    val elementCollidesWithOtherContext =
      vicinities.stream().anyMatch { vicinity -> vicinity.context === vicinity[kind] }
    val allMatchesHaveElement = vicinities.stream().allMatch { vicinity -> vicinity.hasProperty(kind) }
    return allMatchesHaveElement
      && !elementCollidesWithOtherContext
  }

  /**
   * drops the last index if available
   */
  private fun generalizeContextXPath(contexts: List<ArticleContext>, root: Element): GeneralizedContext {
    val vicinity = contexts.map { context ->
      ContextVicinity(
        context = context.contextElement,
        previous = context.contextElement.previousElementSibling(),
        next = context.contextElement.nextElementSibling(),
      )
    }

    val includeNextSibling = includeSibling("next", vicinity)
    val includePreviousSibling = includeSibling("previous", vicinity)

    // console.log('includeNextSibling', includeNextSibling, withSiblings.map(group -> group.next ? group.next.tagName : null));
    // console.log('includePreviousSibling', includePreviousSibling, withSiblings.map(group -> group.previous ? group.previous.tagName : null));

    return GeneralizedContext(
      contextXPath = "//" + __generalizeXPaths(contexts.map { context ->
        getRelativeXPath(
          context.contextElement,
          root
        )
      }),
      extendContext = getContextExtension(includeNextSibling, includePreviousSibling)
    )
  }

  private fun words(text: String): List<String> = text.split(" ")
    .filterTo(ArrayList()) { word: String -> word.length > 0 }

  private fun scoreRule(selectors: GenericFeedSelectors): GenericFeedSelectors {
//    todo mag measure coverage in terms of 1) node count and 2) text coverage in comparison to the rest
    /*
         Here the scoring measure represents how good article rule or feed candidate is in order to be used
         in a feed. In part 1 below the scoring function uses features from the context of a rule - the
         semantics of the elements it is embedded into - , internal features - text length, link count
         and in part 2, the confidence in comparison with other similar rules.
          */
    // scoring part 1
    val contextPathContains: (String) -> Boolean =
      { s -> selectors.contextXPath.lowercase(Locale.getDefault()).indexOf(s.lowercase(Locale.getDefault())) > -1 }
    val linkPathContains: (String) -> Boolean =
      { s -> selectors.linkXPath.lowercase(Locale.getDefault()).indexOf(s.lowercase(Locale.getDefault())) > -1 }
    val texts =
      selectors.contexts!!.map { context -> applyExtendElement(selectors.extendContext, context.contextElement).text() }
    val linkElementsListPerContext = selectors.contexts.map { context ->
      context.contextElement.select("a[href]").toList()
    }
    val linksPerContext =
      linkElementsListPerContext.map { linkElements -> linkElements.map { elem -> elem.attr("href") } }
    var score = 0.0
    selectors.dateXPath?.let {
      score += 10
    }
    if (contextPathContains("header")) score -= 6
    if (contextPathContains("nav")) score -= 10
    if (contextPathContains("article")) score += 2
    if (contextPathContains("main")) score += 6
    if (contextPathContains("aside")) score -= 4
    if (contextPathContains("footer")) score -= 6
    if (contextPathContains("ul")) score -= 3
    if (linkPathContains("h1")) score += 8
    if (linkPathContains("h2")) score += 4
    if (linkPathContains("h3")) score += 2
    if (linkPathContains("h4")) score++
    if (linkPathContains("strong")) score++
    if (linkPathContains("aside")) score -= 2
    if (linkPathContains("article")) score += 2
    if (linkPathContains("section")) score += 2
    // if (rule.linkPath.toLowerCase() === "a") score --
    if (selectors.contextXPath.lowercase(Locale.getDefault()).endsWith("a")) score -= 5
    if (selectors.linkXPath === "./") score--

    // punish bad link texts
    val linkElements = selectors.contexts.mapNotNull { context ->
      if (selectors.linkXPath == "./") {
        context.contextElement
      } else {
        evaluateXPath(
          selectors.linkXPath,
          context.contextElement
        ).firstOrNull()
      }
    }
    val linkTexts = linkElements
      .map { element -> element.text() }
      .toSet()
    score -= selectors.contexts.size - linkTexts.size

    val linkUrls = linkElements
      .map { element -> element.attr("href") }
      .toSet()
    score -= selectors.contexts.size - linkUrls.size

    // punish multiple links elements
    score =
      score - linkElementsListPerContext.map { linkElementsPerContext -> linkElementsPerContext.size }.average() + 1
    // punish multiple links
    score = score - linksPerContext.map { links -> links.size }.average() + 1
    if (texts.map { text -> words(text).size }.average() < 3) score -= 10
    if (texts.map { text -> text.length }.average() > 150) score += 2
    if (texts.map { text -> text.length }.average() > 450) score += 4
    if (texts.map { text -> text.length }.average() > 450) score += 1
    if (texts.stream().anyMatch { text -> text.length < 50 }) score--
    if (selectors.contexts.size < 4) {
      score -= 5
    } else {
      score += ln(selectors.contexts.size.toDouble()) * 1.5 + 1
    }

    log.debug("Score ${selectors.contextXPath} -> $score")
    return GenericFeedSelectors(
      count = selectors.count,
      score = score,
      contexts = selectors.contexts,
      linkXPath = selectors.linkXPath,
      extendContext = selectors.extendContext,
      contextXPath = selectors.contextXPath,
      dateXPath = selectors.dateXPath
    )
  }

  private fun findLinks(document: Document, options: GenericFeedParserOptions): List<LinkPointer> {
    val body = document.body()
    return document.select("A[href]").stream()
      .filter { element -> toWords(element.text()).size >= options.minWordCountOfLink }
      .filter { element -> !element.attr("href").startsWith("javascript") }
      .map { element ->
        LinkPointer(
          element = element,
//          index = getChildIndex(element.parent()!!),
          path = getRelativeCssPath(element.parent()!!, body, options.strictMode)
        )
      }
      .collect(Collectors.toList())

  }

  private fun findCommonParentElement(groupId: String, linkElements: List<Element>): List<Element> {
    // articles are not necessarily in the same parent, e.g. in two separate lists <ul>

    // first two
    val headWalkUp = findCommonParent(linkElements.subList(0, 2.coerceAtLeast(linkElements.size)))
    log.debug("${groupId} headWalkUp=${headWalkUp}")
    // last two
    val tailWalkUp = findCommonParent(linkElements.subList(0.coerceAtLeast(linkElements.size - 2), linkElements.size))
    log.debug("${groupId} tailWalkUp=${tailWalkUp}")
    return linkElements.map { linkElement -> nthParent(headWalkUp.coerceAtMost(tailWalkUp), linkElement) }
  }

  private fun nthParent(n: Int, element: Element): Element {
    var parent = element
    for (i in 0..n - 1) {
      parent = parent.parent()!!
    }
    return parent
  }

  private fun findCommonParent(nodes: List<Element>): Int {
    var linkElements = nodes
    var up = 0
    while (true) {
      if (linkElements.stream().anyMatch { currentNode -> !currentNode.hasParent() }) {
        break
      }
      val parentNodes = linkElements.map { currentNode -> currentNode.parent() }
      if (parentNodes.isEmpty()) {
        break
      }
      if (parentNodes[0] === parentNodes[1]) {
        break
      }
      up++
      linkElements = parentNodes.filterNotNull()
    }
    return up
  }

  private fun findArticleContext(
    groupId: String,
    linkPointers: List<LinkPointer>,
  ): List<ArticleContext> {
    val linkElements = linkPointers.map { linkPointer -> linkPointer.element }
    val articleRootElements = findCommonParentElement(groupId, linkElements)

    return linkPointers.mapIndexed { index, linkPointer ->
      ArticleContext(
        id = groupId,
        linkElement = linkPointer.element,
        contextElement = articleRootElements[index],
        dateElement = null
      )
    }
  }

  private fun convertContextsToRule(contexts: List<ArticleContext>, root: Element): GenericFeedSelectors {
    val linkXPath = "./" + __generalizeXPaths(contexts.map { context ->
      getRelativeXPath(
        context.linkElement,
        context.contextElement
      )
    }.toSet())
    val dateXPath = if (contexts.first().dateElement !== null) {
      "./" + __generalizeXPaths(contexts.map { context ->
        getRelativeXPath(
          context.dateElement!!,
          context.contextElement
        )
      }.toSet())
    } else {
      null
    }
    val generalizeContext = generalizeContextXPath(contexts, root)
    return GenericFeedSelectors(
      count = contexts.size,
      score = 0.0,
      contexts = contexts,
      extendContext = generalizeContext.extendContext,
      linkXPath = linkXPath,
      dateXPath = dateXPath,
      contextXPath = generalizeContext.contextXPath
    )
  }

  private fun withAbsUrls(element: Element, url: URL): Element {
    element.select("a[href]")
      .filter { link -> !link.attr("href").startsWith("javascript") }
      .forEach { link ->
        link.attr("href", toAbsoluteUrl(url, link.attr("href")))
      }
    element.select("img[src]")
      .filter { img -> !img.attr("src").startsWith("data:") }
      .forEach { link ->
        link.attr("src", toAbsoluteUrl(url, link.attr("src")))
      }
    return element
  }

}

fun toDto(it: PuppeteerWaitUntil): org.migor.feedless.generated.types.PuppeteerWaitUntil {
  return when (it) {
    PuppeteerWaitUntil.domcontentloaded -> org.migor.feedless.generated.types.PuppeteerWaitUntil.domcontentloaded
    PuppeteerWaitUntil.networkidle2 -> org.migor.feedless.generated.types.PuppeteerWaitUntil.networkidle2
    PuppeteerWaitUntil.networkidle0 -> org.migor.feedless.generated.types.PuppeteerWaitUntil.networkidle0
    PuppeteerWaitUntil.load -> org.migor.feedless.generated.types.PuppeteerWaitUntil.load
  }
}
