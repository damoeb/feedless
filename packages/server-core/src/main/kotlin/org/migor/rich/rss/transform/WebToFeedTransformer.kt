package org.migor.rich.rss.transform

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.rich.rss.api.WebToFeedParams
import org.migor.rich.rss.api.dto.RichArticle
import org.migor.rich.rss.harvest.ArticleRecoveryType
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.util.FeedUtil
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
  val extendContext: String
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
      else -> throw RuntimeException("$propertyName not available")
    }
  }

  fun hasProperty(propertyName: String): Boolean {
    return try {
      Optional.ofNullable(get(propertyName)).map { true }.orElse(false)
    } catch (e: RuntimeException) {
      false
    }

  }
}

data class LinkPointer(
  val element: Element,
//  val index: Int,
  val path: String
)

abstract class FeedRule {
  abstract val linkXPath: String
  abstract val extendContext: String
  abstract val contextXPath: String
  abstract val dateXPath: String?
}

data class GenericFeedRule(
  override val linkXPath: String,
  override val extendContext: String,
  override val contextXPath: String,
  override val dateXPath: String?,
  val feedUrl: String,
  val count: Int?,
  val score: Double,
  val samples: List<RichArticle> = emptyList()
) : FeedRule()

data class GenericFeedParserOptions(
  val strictMode: Boolean = false,
  val eventFeed: Boolean = false,
  val version: String,
)

data class GenericFeedFetchOptions(
  val websiteUrl: String,
  val prerender: Boolean = false,
  var prerenderDelayMs: Int = 0,
  var prerenderWithoutMedia: Boolean = false,
  var prerenderScript: String? = null
)

data class GenericFeedRefineOptions(
  val filter: String = "",
  val recovery: ArticleRecoveryType = ArticleRecoveryType.NONE,
)

data class GenericFeedSelectors(
  val count: Int? = null,
  val score: Double? = null,
  val contexts: List<ArticleContext>? = null,
  override val linkXPath: String,
  override val extendContext: String,
  override val contextXPath: String,
  override val dateXPath: String? = null
) : FeedRule()

data class GenericFeedSpecification(
  val selectors: GenericFeedSelectors?,
  val parserOptions: GenericFeedParserOptions,
  val fetchOptions: GenericFeedFetchOptions,
  val refineOptions: GenericFeedRefineOptions,
)

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

  private val minLinkGroupSize = 2
  private val minWordCountOfLink = 1
  private val reLinebreaks = Regex("^[\n\t\r ]+|[\n\t\r ]+$")
  private val reXpathId = Regex("(.*)\\[@id=(.*)\\]")
  private val reXpathIndexNode = Regex("([^\\[]+)\\[([0-9]+)\\]?")

  fun getArticleRules(
    corrId: String,
    document: Document,
    url: URL,
    articleRecovery: ArticleRecoveryType,
    strictMode: Boolean,
    sampleSize: Int = 0
  ): List<GenericFeedRule> {
    val body = document.body()

    val linkElements: List<LinkPointer> = findLinks(document, strictMode).distinctBy { it.element.attr("href") }

    // group links with similar path in document
    val groupedLinks = groupLinksByPath(linkElements)

    log.debug("Found ${groupedLinks.size} link groups with strictMode=${strictMode}")

    val parserOptions = GenericFeedParserOptions(
      strictMode = strictMode,
      version = ""
    )
    val fetchOptions = GenericFeedFetchOptions(
      websiteUrl = url.toString(),
    )
    val refineOptions = GenericFeedRefineOptions(
      recovery = articleRecovery
    )

    return groupedLinks
      .mapTo(mutableListOf()) { entry -> Pair(entry.key, entry.value) }
      .filter { (groupId, linksInGroup) -> hasRelevantSize(groupId, linksInGroup) }
      .map { (groupId, linksInGroup) -> findArticleContext(groupId, linksInGroup) }
      .map { contexts -> tryAddDateXPath(contexts) }
      .map { contexts -> convertContextsToRule(contexts, body) }
      .map { selectors -> scoreRule(selectors) }
      .sortedByDescending { it.score }
      .map { selectors ->
        GenericFeedRule(
          feedUrl = createFeedUrl(url, selectors, parserOptions, fetchOptions, refineOptions),
          count = selectors.count,
          score = selectors.score!!,
          linkXPath = selectors.linkXPath,
          extendContext = selectors.extendContext,
          contextXPath = selectors.contextXPath,
          dateXPath = selectors.dateXPath,
          samples = getArticlesByRule(corrId, selectors, document, url, sampleSize)
        )
      }
      .toList()
  }

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

  private fun hasRelevantSize(groupId: String, linksInGroup: MutableList<LinkPointer>): Boolean {
    val hasEnoughMembers = linksInGroup.size >= minLinkGroupSize

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
    selectors: FeedRule,
    parserOptions: GenericFeedParserOptions,
    fetchOptions: GenericFeedFetchOptions,
    refineOptions: GenericFeedRefineOptions
  ): String {
    val encode: (value: String) -> String = { value -> URLEncoder.encode(value, StandardCharsets.UTF_8) }
    val params = mapOf(
      WebToFeedParams.version to propertyService.webToFeedVersion,
      WebToFeedParams.url to url.toString(),
      WebToFeedParams.linkPath to selectors.linkXPath,
      WebToFeedParams.contextPath to selectors.contextXPath,
      WebToFeedParams.datePath to StringUtils.trimToEmpty(selectors.dateXPath),
      WebToFeedParams.extendContext to selectors.extendContext,
      WebToFeedParams.prerender to fetchOptions.prerender,
      WebToFeedParams.prerenderScript to fetchOptions.prerenderScript,
      WebToFeedParams.prerenderWaitMs to fetchOptions.prerenderDelayMs,
      WebToFeedParams.filter to refineOptions.filter,
      WebToFeedParams.articleRecovery to refineOptions.recovery,
    ).map { entry -> entry.key to encode("${entry.value}") }

    val searchParams = params.fold("") { acc, pair -> acc + "${pair.first}=${pair.second}&" }
    return "${propertyService.publicUrl}/api/web-to-feed?$searchParams"
  }

  fun getArticlesByRule(
    corrId: String,
    rule: FeedRule,
    document: Document,
    url: URL,
    sampleSize: Int = 0
  ): List<RichArticle> {

    val now = Date()
    val locale = extractLocale(document, propertyService.locale)
    log.debug("[${corrId}] getArticlesByRule context=${rule.contextXPath} link=${rule.linkXPath} date=${rule.dateXPath}")
    val articles = evaluateXPath(rule.contextXPath, document).mapNotNull { element ->
      try {
        val content = applyExtendElement(rule.extendContext, element)
        val link = evaluateXPath(rule.linkXPath, element).firstOrNull()
        link?.let {
          val pubDate =
            Optional.ofNullable(StringUtils.trimToNull(rule.dateXPath))
              .map { dateXPath -> Optional.ofNullable(extractPubDate(corrId, dateXPath, element, locale)).orElse(now) }
              .orElse(now)
          val linkText = link.text()
          val articleUrl = toAbsoluteUrl(url, link.attr("href"))

          val article = RichArticle(
            id = FeedUtil.toURI("article", articleUrl),
            title = linkText.replace(reLinebreaks, " "),
            url = articleUrl,
            contentText = webToTextTransformer.extractText(content),
            contentRaw = withAbsUrls(content, url).selectFirst("div")!!.outerHtml(),
            contentRawMime = "text/html",
            publishedAt = pubDate,
            imageUrl = null
          )

          if (qualifiesAsArticle(element, rule)) {
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

  private fun applyExtendElement(extendContext: String, element: Element): Element {
    val p =
      if (extendContext.indexOf(ExtendContext.PREVIOUS.value) > -1) element.previousElementSibling()
        ?.outerHtml() else ""
    val n =
      if (extendContext.indexOf(ExtendContext.NEXT.value) > -1) element.nextElementSibling()?.outerHtml() else ""
    return Jsoup.parse("<div>${p}${element.outerHtml()}${n}</div>")
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
    return text.trim().split(" ").filter { word -> word.isNotEmpty() }
  }

  private fun toAbsoluteUrl(url: URL, link: String): String {
    return URL(url, link).toString()
  }

  private fun qualifiesAsArticle(elem: Element, rule: FeedRule): Boolean {
    if (elem.text()
        .replace("\r", "")
        .replace("\n", "")
        .replace("\t", "")
        .isEmpty()
    ) {
      return false
    }
    val links = evaluateXPath(rule.linkXPath, elem)
    return links.isNotEmpty()

  }

  fun __generalizeXPaths(xpaths: Collection<String>): String {
    val tokenized = xpaths.map { xpath ->
      xpath.split('/')
        .filter { xpathFragment -> xpathFragment.isNotEmpty() }
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

  private fun getContextExtension(includeNextSibling: Boolean, includePreviousSibling: Boolean): String {
    // node 0
    // prev 1
    // next 2
    // prev+next 3

    var extendContext = ""
    if (includePreviousSibling) {
      extendContext += ExtendContext.PREVIOUS.value
    }
    if (includeNextSibling) {
      extendContext += ExtendContext.NEXT.value
    }
    return extendContext
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
    throw RuntimeException("Cannot generate xpath")
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

  private fun words(text: String): List<String> = text.split(" ").filter { word -> word.length > 0 }

  private fun scoreRule(rule: GenericFeedSelectors): GenericFeedSelectors {
//    todo mag measure coverage in terms of 1) node count and 2) text coverage in comparison to the rest
    /*
         Here the scoring measure represents how good article rule or feed candidate is in order to be used
         in a feed. In part 1 below the scoring function uses features from the context of a rule - the
         semantics of the elements it is embedded into - , internal features - text length, link count
         and in part 2, the confidence in comparison with other similar rules.
          */
    // scoring part 1
    val contextPathContains: (String) -> Boolean =
      { s -> rule.contextXPath.lowercase(Locale.getDefault()).indexOf(s.lowercase(Locale.getDefault())) > -1 }
    val linkPathContains: (String) -> Boolean =
      { s -> rule.linkXPath.lowercase(Locale.getDefault()).indexOf(s.lowercase(Locale.getDefault())) > -1 }
    val texts = rule.contexts!!.map { context -> applyExtendElement(rule.extendContext, context.contextElement).text() }
    val linkElementsListPerContext = rule.contexts.map { context ->
      context.contextElement.select("a[href]").toList()
    }
    val linksPerContext =
      linkElementsListPerContext.map { linkElements -> linkElements.map { elem -> elem.attr("href") } }
    var score = 0.0
    rule.dateXPath?.let {
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
    if (rule.contextXPath.lowercase(Locale.getDefault()).endsWith("a")) score -= 5
    if (rule.linkXPath === "./") score--

    // punish bad link texts
    val linkElements = rule.contexts.mapNotNull { context ->
      if (rule.linkXPath == "./") {
        context.contextElement
      } else {
        evaluateXPath(
          rule.linkXPath,
          context.contextElement
        ).firstOrNull()
      }
    }
    val linkTexts = linkElements
      .map { element -> element.text() }
      .toSet()
    score -= rule.contexts.size - linkTexts.size

    val linkUrls = linkElements
      .map { element -> element.attr("href") }
      .toSet()
    score -= rule.contexts.size - linkUrls.size

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
    if (rule.contexts.size < 4) {
      score -= 5
    } else {
      score += ln(rule.contexts.size.toDouble()) * 1.5 + 1
    }

    log.debug("Score ${rule.contextXPath} -> $score")
    return GenericFeedSelectors(
      count = rule.count,
      score = score,
      contexts = rule.contexts,
      linkXPath = rule.linkXPath,
      extendContext = rule.extendContext,
      contextXPath = rule.contextXPath,
      dateXPath = rule.dateXPath
    )
  }

  private fun findLinks(document: Document, strictMode: Boolean): List<LinkPointer> {
    val body = document.body()
    return document.select("A[href]").stream()
      .filter { element -> toWords(element.text()).size >= minWordCountOfLink }
      .filter { element -> !element.attr("href").startsWith("javascript") }
      .map { element ->
        LinkPointer(
          element = element,
//          index = getChildIndex(element.parent()!!),
          path = getRelativeCssPath(element.parent()!!, body, strictMode)
        )
      }
      .collect(Collectors.toList())

  }

  private fun findArticleRootElement(groupId: String, linkElements: List<Element>): List<Element> {
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
    val articleRootElements = findArticleRootElement(groupId, linkElements)

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

  private fun getChildIndex(element: Element): Int {
    var index = 0
    var child = element
    while (child.previousElementSibling() != null) {
      index++
      child = child.previousElementSibling()!!
    }
    return index
  }

  private fun withAbsUrls(element: Element, url: URL): Element {
    element.select("a[href]")
      .filter { link -> !link.attr("href").startsWith("javascript") }
      .forEach { link ->
        link.attr("href", toAbsoluteUrl(url, link.attr("href")))
      }
    return element
  }
}
