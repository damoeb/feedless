package org.migor.feedless.scrape

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.common.PropertyService
import org.migor.feedless.feed.parser.json.JsonFeed
import org.migor.feedless.feed.parser.json.JsonItem
import org.migor.feedless.generated.types.DOMElementByXPath
import org.migor.feedless.generated.types.DOMExtract
import org.migor.feedless.generated.types.ScrapeEmit
import org.migor.feedless.generated.types.ScrapeExtractFragment
import org.migor.feedless.generated.types.ScrapeExtractResponse
import org.migor.feedless.util.FeedUtil
import org.migor.feedless.util.HtmlUtil.parseHtml
import org.migor.feedless.util.toLocalDateTime
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import us.codecraft.xsoup.Xsoup
import java.net.URI
import java.time.LocalDateTime
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
  abstract val paginationXPath: String?
  abstract val dateIsStartOfEvent: Boolean
}

data class GenericFeedRule(
  override val linkXPath: String,
  override val extendContext: ExtendContext,
  override val contextXPath: String,
  override val dateXPath: String?,
  override val paginationXPath: String?,
  override val dateIsStartOfEvent: Boolean = false,
  val count: Int = 0,
  val score: Double,
) : Selectors()

@JsonIgnoreProperties
data class GenericFeedParserOptions(
  val minLinkGroupSize: Int = 2,
  val minWordCountOfLink: Int = 1,
)

enum class PuppeteerWaitUntil {
  networkidle0,
  networkidle2,
  load,
  domcontentloaded

}

@JsonIgnoreProperties
data class GenericFeedSelectors(
  val count: Int? = null,
  val score: Double? = null,
  val contexts: List<ArticleContext>? = null,
  override val linkXPath: String,
  override val extendContext: ExtendContext,
  override val contextXPath: String,
  override val dateXPath: String? = null,
  override val paginationXPath: String? = null,
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
@Transactional(propagation = Propagation.NEVER)
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class WebToFeedTransformer(
  @Autowired
  private var propertyService: PropertyService,
  @Autowired
  private var webToTextTransformer: WebToTextTransformer,
  @Autowired
  private var webExtractService: WebExtractService
) {

  private val log = LoggerFactory.getLogger(WebToFeedTransformer::class.simpleName)

  private val reLinebreaks = Regex("^[\n\t\r ]+|[\n\t\r ]+$")
  private val reXpathId = Regex("(.*)\\[@id=(.*)\\]")
  private val reXpathIndexNode = Regex("([^\\[]+)\\[([0-9]+)\\]?")

  suspend fun parseFeedRules(
    document: Document,
    uri: URI,
    parserOptions: GenericFeedParserOptions,
  ): List<GenericFeedRule> {
    val body = document.body()

    val linkElements: List<LinkPointer> = findLinks(document, parserOptions).distinctBy { it.element.attr("href") }

    // group links with similar path in document
    val linkGroups = groupLinksByPath(linkElements)

    log.debug("Found ${linkGroups.size} link groups")

    val paginationXPath = findPaginationXPath(linkGroups, uri.toURL().toString(), document)

    return linkGroups
      .asSequence()
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
          count = selectors.count ?: 0,
          score = selectors.score!!,
          linkXPath = selectors.linkXPath,
          extendContext = selectors.extendContext,
          contextXPath = selectors.contextXPath,
          dateXPath = selectors.dateXPath,
          paginationXPath = paginationXPath,
        )
      }
      .toList()
  }

  private suspend fun findPaginationXPath(
    groupedLinks: HashMap<String, MutableList<LinkPointer>>,
    url: String,
    document: Document
  ): String? {
    return findPaginationElement(groupedLinks, url)
      ?.let { "/" + getRelativeXPath(it, document.body()) }
  }

  private suspend fun findPaginationElement(
    groupedLinks: HashMap<String, MutableList<LinkPointer>>,
    url: String
  ): Element? {
    val ed = org.apache.commons.text.similarity.LevenshteinDistance(1)
    return groupedLinks
      .values
      .filter { it.size > 2 }
      .mapNotNull {
        try {
          Pair(it, it.map {
            ed.apply(toAbsoluteUrl(URI(url), it.element.attr("href")).toURL().toString(), url)
          }.average())
        } catch (e: Exception) {
          null
        }
      }
      .sortedBy { listAndEditDistance -> listAndEditDistance.second }
      .filter { it.second < 4 }
      .map { it.first }
      .map { linkPointers ->
        findCommonParentElement("", linkPointers.map { it.element }).distinct()
      }
      .filter { it.size == 1 }
      .map { it.first() }
      .firstOrNull()
  }

  private suspend fun tryAddDateXPath(contexts: List<ArticleContext>): List<ArticleContext> {
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

  private suspend fun hasRelevantSize(
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

  private suspend fun groupLinksByPath(linkElements: List<LinkPointer>) =
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

  suspend fun getFeedBySelectors(
    selectors: Selectors,
    document: Document,
    uri: URI,
    logger: LogCollector,
  ): JsonFeed {
    val locale = extractLocale(document, propertyService.locale)
    val element = withAbsUrls(document, uri)
    val response = webExtractService.extract(
      selectors.toScrapeExtracts(),
      element,
      locale,
      logger
    )
    logger.log("Extracting pagination xpath ${selectors.paginationXPath}")
    val links = StringUtils.trimToNull(selectors.paginationXPath)?.let {
      val xpath = if (it.matches(Regex("a(\\[[0-9]+\\])?$", RegexOption.IGNORE_CASE))) {
        it
      } else {
        "$it//a/@href"
      }
      logger.log("pagination xpath $xpath")
      webExtractService.extract(
        DOMExtract(
          fragmentName = "next",
          xpath = DOMElementByXPath(value = xpath),
          max = 10,
          emit = listOf(ScrapeEmit.text)
        ),
        element,
        locale,
        logger,
      )
    }
    return convertExtractsToJsonFeed(response, links, uri, logger, StringUtils.isNotBlank(selectors.linkXPath))
  }

  suspend fun getArticlesBySelectors(
    selectors: Selectors,
    document: Document,
    uri: URI,
  ): List<JsonItem> {
    return getFeedBySelectors(selectors, document, uri, LogCollector()).items
  }

  private suspend fun convertExtractsToJsonFeed(
    feed: ScrapeExtractResponse,
    links: ScrapeExtractResponse?,
    url: URI,
    logger: LogCollector,
    distinctUrls: Boolean
  ): JsonFeed {

    val items = feed.fragments!!
      .map { convertExtractToJsonItem(it, url) }

    logger.log("Create feed with items [\n${items.map { "\t${it.title} -> ${it.url}\n" }}\n]")

    val jsonFeed = JsonFeed()
    jsonFeed.id = ""
    jsonFeed.title = "Feed"
    jsonFeed.websiteUrl = ""
    jsonFeed.publishedAt = LocalDateTime.now()
    jsonFeed.items = if (distinctUrls) {
      items
        .distinctBy { it.url }
    } else {
      items
    }
    jsonFeed.feedUrl = ""
    links?.let {
      jsonFeed.links = links.fragments!!.mapNotNull { it.data?.data }
        .map { it.replace(Regex("#.*"), "") }
        .distinct()
    }

    return jsonFeed
  }

  private suspend fun convertExtractToJsonItem(fragment: ScrapeExtractFragment, baseUrl: URI): JsonItem {
    val element = parseHtml(fragment.html!!.data, baseUrl.toString()).body()
    val text = fragment.text!!.data

    val item = JsonItem()
    item.id = FeedUtil.toURI("article", text)
    item.title = StringUtils.substring(text.replace(reLinebreaks, " "), 0, 100)
    val url =
      fragment.extracts?.find { it.fragmentName == JsonItem.URL }?.fragments?.find { it.data?.mimeType == WebExtractService.MIME_URL }?.data?.data
    item.url = url ?: ""

    item.text = webToTextTransformer.extractText(element)
    item.rawBase64 = withAbsUrls(element, baseUrl).selectFirst("body")!!.html()
    item.rawMimeType = "text/html"
    item.publishedAt = LocalDateTime.now()

    val tryExtractDate =
      { f: ScrapeExtractResponse -> f.fragments!!.firstOrNull()?.data?.data?.toLong()?.toLocalDateTime() }

    fragment.extracts?.find { childFragment -> childFragment.fragmentName == JsonItem.PUBLISHED_AT }?.let {
      item.publishedAt = tryExtractDate(it) ?: LocalDateTime.now()
    }

    fragment.extracts?.find { childFragment -> childFragment.fragmentName == JsonItem.STARTING_AT }?.let {
      item.startingAt = tryExtractDate(it)
    }
    return item
  }

  private suspend fun extractLocale(document: Document, fallback: Locale): Locale {
    val langStr = document.select("html").attr("lang")
    return Optional.ofNullable(StringUtils.trimToNull(langStr))
      .map {
        run {
          log.debug("Detected locale $it")
          Locale.forLanguageTag(it)
        }
      }
      .orElse(fallback)
  }

  private suspend fun applyExtendElement(extendContext: ExtendContext, element: Element): Element {
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


  private suspend fun getRelativeCssPath(nodeParam: Element, context: Element): String {
    if (nodeParam == context) {
      // todo mag this is not applicable
      return "self"
    }
    var node = nodeParam
    var path = node.tagName() // tagName for text nodes is undefined
    while (node.parentNode() !== context && node.hasParent()) {
      node = node.parent()!!
      path = "${getNodeName(node)}>${path}"
    }
    return path
  }

  private suspend fun getNodeName(node: Element): String {
//    return if (strictMode) {
//      var childId = 0
//      var ps = node.previousElementSibling()
//      while (ps != null) {
//        childId++
//        ps = ps.previousElementSibling()
//      }
//
//      node.tagName() + childId
//    } else {
    return node.tagName()
//    }
  }

  private suspend fun toWords(text: String): List<String> {
    return text.trim().split(" ").filterTo(ArrayList()) { word: String -> word.isNotEmpty() }
  }

  suspend fun __generalizeXPaths(xpaths: Collection<String>): String {
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
          val changingIndex = try {
            tokenized.stream()
              .map { otherXpathSegment -> otherXpathSegment[index].match!!.index }
              .collect(Collectors.toSet()).size > 1
          } catch (e: Exception) {
            false
          }
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

  private suspend fun getContextExtension(includeNextSibling: Boolean, includePreviousSibling: Boolean): ExtendContext {
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

  suspend fun getRelativeXPath(element: Element, context: Element): String {

    if (element === context) {
      return ""
    }

    val xpath = StringBuilder()

    var currentElement: Element? = element
    while (currentElement != null && currentElement != context) {
      val index: Int = getElementIndex(currentElement)
      val tagName = currentElement.tagName()

      if (index > 1) {
        xpath.insert(0, "/$tagName[$index]")
      } else {
        xpath.insert(0, "/$tagName")
      }

      currentElement = currentElement.parent()!!
    }

    return if (xpath.isNotEmpty()) xpath.insert(0, "/")
      .toString() else throw IllegalArgumentException("Cannot generate xpath")
  }

  fun getElementIndex(element: Element): Int {
    var index = 1
    var sibling = element.previousElementSibling()

    while (sibling != null) {
      if (sibling.tagName() == element.tagName()) {
        index++
      }
      sibling = sibling.previousElementSibling()
    }

    return index
  }

  private suspend fun evaluateXPath(xpath: String, context: Element): List<Element> {
    return if (xpath == "./") {
      listOf(context)
    } else {
      val xpathResult = Xsoup.compile(xpath.replaceFirst("./", "//")).evaluate(context).elements
      xpathResult.toList()
    }
  }

  private suspend fun includeSibling(kind: String, vicinities: List<ContextVicinity>): Boolean {
    val elementCollidesWithOtherContext =
      vicinities.stream().anyMatch { vicinity -> vicinity.context === vicinity[kind] }
    val allMatchesHaveElement = vicinities.stream().allMatch { vicinity -> vicinity.hasProperty(kind) }
    return allMatchesHaveElement
      && !elementCollidesWithOtherContext
  }

  /**
   * drops the last index if available
   */
  private suspend fun generalizeContextXPath(contexts: List<ArticleContext>, root: Element): GeneralizedContext {
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

  private suspend fun words(text: String): List<String> = text.split(" ")
    .filterTo(ArrayList()) { word: String -> word.isNotEmpty() }

  private suspend fun scoreRule(selectors: GenericFeedSelectors): GenericFeedSelectors {
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

  private suspend fun findLinks(document: Document, options: GenericFeedParserOptions): List<LinkPointer> {
    val body = document.body()
//    return document.select("A[href],AREA[href]").stream()
    return document.select("A[href]")
      .filter { element -> toWords(element.text()).size >= options.minWordCountOfLink }
      .filter { element -> !element.attr("href").startsWith("javascript") }
      .map { element ->
        LinkPointer(
          element = element,
//          index = getChildIndex(element.parent()!!),
          path = getRelativeCssPath(element.parent()!!, body)
        )
      }
  }

  private suspend fun findCommonParentElement(groupId: String, linkElements: List<Element>): List<Element> {
    // articles are not necessarily in the same parent, e.g. in two separate lists <ul>

    // first two
    val headWalkUp = findCommonParent(linkElements.subList(0, 2.coerceAtLeast(linkElements.size)))
    log.debug("$groupId headWalkUp=${headWalkUp}")
    // last two
    val tailWalkUp = findCommonParent(linkElements.subList(0.coerceAtLeast(linkElements.size - 2), linkElements.size))
    log.debug("$groupId tailWalkUp=${tailWalkUp}")
    return linkElements.map { linkElement -> nthParent(headWalkUp.coerceAtMost(tailWalkUp), linkElement) }
  }

  private suspend fun nthParent(n: Int, element: Element): Element {
    var parent = element
    for (i in 0..n - 1) {
      parent = parent.parent()!!
    }
    return parent
  }

  private suspend fun findCommonParent(nodes: List<Element>): Int {
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

  private suspend fun findArticleContext(
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

  private suspend fun convertContextsToRule(contexts: List<ArticleContext>, root: Element): GenericFeedSelectors {
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
//    assert(Xsoup.compile(generalizeContext.contextXPath).evaluate(root).elements.isNotEmpty())
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

  companion object {
    fun toAbsoluteUrl(base: URI, maybeRelativeLink: String): URI {
      return toAbsoluteUrl(base, URI.create(maybeRelativeLink))
    }

    private fun toAbsoluteUrl(uri: URI, maybeRelativeLink: URI): URI {
      return if (maybeRelativeLink.isAbsolute) {
        maybeRelativeLink
      } else {
        uri.resolve(maybeRelativeLink)
      }
    }

    fun withAbsUrls(element: Element, url: URI): Element {
      element.select("a[href]")
        .filter { link -> !link.attr("href").startsWith("javascript") }
        .forEach { link ->
          try {
            link.attr("href", toAbsoluteUrl(url, link.attr("href")).toURL().toString())
          } catch (_: Exception) {

          }
        }
      element.select("img[src]")
        .filter { img -> !img.attr("src").startsWith("data:") }
        .forEach { link ->
          link.attr("src", toAbsoluteUrl(url, link.attr("src")).toURL().toString())
        }
      return element
    }


//    fun normalizeTags(element: Element): Element {
//      element.select("table").tagName("div").attr("role", "table")
//      element.select("thead").tagName("div").attr("role", "thead")
//      element.select("tbody").tagName("div").attr("role", "tbody")
//      element.select("tfoot").tagName("div").attr("role", "tfoot")
//      element.select("tr").tagName("div").attr("role", "row")
//      element.select("td").tagName("div").attr("role", "cell")
//      element.select("font").tagName("div").attr("role", "font")
//      element.select("form").tagName("div").attr("role", "form")
//      return element
//    }
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

private fun Selectors.toScrapeExtracts(): DOMExtract {
  val extracts = mutableListOf(
    DOMExtract(
      fragmentName = JsonItem.URL,
      xpath = DOMElementByXPath(value = linkXPath),
      max = 1,
      emit = listOf(ScrapeEmit.text, ScrapeEmit.html)
    )
  )
  if (StringUtils.isNotBlank(dateXPath)) {
    val fragmentName = if (dateIsStartOfEvent) {
      JsonItem.STARTING_AT
    } else {
      JsonItem.PUBLISHED_AT
    }
    extracts.add(
      DOMExtract(
        fragmentName = fragmentName,
        xpath = DOMElementByXPath(value = dateXPath!!),
        max = 1,
        emit = listOf(ScrapeEmit.date, ScrapeEmit.html)
      )
    )
  }

  return DOMExtract(
    fragmentName = "feed",
    xpath = DOMElementByXPath(value = contextXPath),
    emit = listOf(ScrapeEmit.html, ScrapeEmit.text),
    extract = extracts
  )
}
