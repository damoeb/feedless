package org.migor.rss.rich.transform

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.rss.rich.api.dto.ArticleJsonDto
import org.migor.rss.rich.service.PropertyService
import org.migor.rss.rich.util.FeedUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import us.codecraft.xsoup.Xsoup
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.Collectors


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
      get(propertyName) != null
    } catch (e: RuntimeException) {
      false
    }

  }
}

data class LinkPointer(
  val element: Element,
  val index: Int,
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
  val samples: List<ArticleJsonDto> = emptyList()
) : FeedRule()

data class CandidateFeedRule(
  val count: Int? = null,
  val score: Double? = null,
  val contexts: List<ArticleContext>? = null,
  override val linkXPath: String,
  override val extendContext: String,
  override val contextXPath: String,
  override val dateXPath: String?
) : FeedRule()

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
  private var webToTextTransformer: WebToTextTransformer
) {

  private val log = LoggerFactory.getLogger(WebToFeedTransformer::class.simpleName)

  private val minLinkGroupSize = 2
  private val minWordCountOfLink = 1
  private val reLinebreaks = Regex("^[\n\t\r ]+|[\n\t\r ]+$")
  private val reNumber = Regex("[0-9]+")
  private val reXpathId = Regex("(.*)\\[@id=(.*)\\]")
  private val reXpathIndexNode = Regex("([^\\[]+)\\[([0-9]+)\\]?")

  // credits https://stackoverflow.com/a/3390252
  private val dateFormatToRegexp = listOf(
    Triple(toRegex("^\\d{8}$"), "yyyyMMdd", false),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}$"), "dd MM yyyy", false),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}$"), "yyyy MM dd", false),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}$"), "MM dd yyyy", false),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}$"), "yyyy MM dd", false),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}$"), "dd MMM yyyy", false),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}$"), "dd MMMM yyyy", false),
    Triple(toRegex("^[a-z]{3,}\\s\\d{1}\\s\\d{4}$"), "MMMM d yyyy", false), // December 8, 2020
    Triple(toRegex("^[a-z]{3,}\\s\\d{2}\\s\\d{4}$"), "MMMM dd yyyy", false), // December 15, 2020
    Triple(toRegex("^\\d{12}$"), "yyyyMMddHHmm", true),
    Triple(toRegex("^\\d{8}\\s\\d{4}$"), "yyyyMMdd HHmm", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"), "dd MM yyyy HH:mm", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}$"), "yyyy MM dd HH:mm", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"), "MM dd yyyy HH:mm", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}$"), "yyyy MM dd HH:mm", true),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"), "dd MMM yyyy HH:mm", true),
    Triple(
      toRegex("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}$"),
      "dd MMMM yyyy HH:mm",
      true
    ), // 06. Januar 2022, 08:00 Uhr
    Triple(toRegex("^\\d{14}$"), "yyyyMMddHHmmss", true),
    Triple(toRegex("^\\d{8}\\s\\d{6}$"), "yyyyMMdd HHmmss", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "dd MM yyyy HH:mm:ss", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "yyyy MM dd HH:mm:ss", true),
    Triple(toRegex("^\\d{1,2}\\s\\d{1,2}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "MM dd yyyy HH:mm:ss", true),
    Triple(toRegex("^\\d{4}\\s\\d{1,2}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "yyyy MM dd HH:mm:ss", true),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "dd MMM yyyy HH:mm:ss", true),
    Triple(toRegex("^\\d{1,2}\\s[a-z]{4,}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"), "dd MMMM yyyy HH:mm:ss", true),
  )

  private fun toRegex(regex: String): Regex {
    return Regex(regex, RegexOption.IGNORE_CASE)
  }

  fun getArticleRules(corrId: String, document: Document, url: URL, sampleSize: Int = 0): List<GenericFeedRule> {
    val body = document.body()

    val linkElements: List<LinkPointer> = findLinks(document)

    // group links with similar path in document
    val groupedLinks = groupLinksByPath(linkElements)

    log.debug("Found ${groupedLinks.size} link groups")

    return groupedLinks
      .mapTo(mutableListOf()) { entry -> Pair(entry.key, entry.value) }
      .filter { (groupId, linksInGroup) -> hasRelevantSize(groupId, linksInGroup) }
      .map { (groupId, linksInGroup) -> findArticleContext(groupId, linksInGroup) }
      .map { contexts -> tryAddDateXPath(contexts) }
      .map { contexts -> convertContextsToRule(contexts, body) }
      .map { rule -> scoreRule(rule) }
      .sortedByDescending { it.score }
      .map { rule ->
        GenericFeedRule(
          feedUrl = convertRuleToFeedUrl(url, rule),
          count = rule.count,
          score = rule.score!!,
          linkXPath = rule.linkXPath,
          extendContext = rule.extendContext,
          contextXPath = rule.contextXPath,
          dateXPath = rule.dateXPath,
          samples = getArticlesByRule(corrId, rule, document, url, sampleSize)
        )
      }
      .toList()
  }

  private fun tryAddDateXPath(contexts: List<ArticleContext>): List<ArticleContext> {
    val hasTimeField = contexts.all { context -> context.contextElement.selectFirst("time") != null }

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
      log.debug("Keeping $groupId (${linksInGroup.size} links)")
    } else {
      log.debug("Dropping $groupId, (${linksInGroup.size} links)")
    }

    return hasEnoughMembers
  }

  private fun groupLinksByPath(linkElements: List<LinkPointer>) =
    linkElements.fold(HashMap<String, MutableList<LinkPointer>>()) { linkGroup, linkPath ->
      run {
        val groupId = linkPath.path + linkPath.index
        if (!linkGroup.containsKey(groupId)) {
          linkGroup[groupId] = mutableListOf()
        }
        linkGroup[groupId]!!.add(linkPath)
        //        this.log.debug("group $groupId add ${linkPath.index}")
        linkGroup
      }
    }

  fun convertRuleToFeedUrl(url: URL, rule: FeedRule): String {
    val encode: (value: String) -> String = { value -> URLEncoder.encode(value, StandardCharsets.UTF_8) }
    return "${propertyService.host}/api/web-to-feed?version=${propertyService.webToFeedVersion}&url=${encode(url.toString())}&linkXPath=${
      encode(
        rule.linkXPath
      )
    }&extendContext=${
      encode(
        rule.extendContext
      )
    }&contextXPath=${encode(rule.contextXPath)}" + Optional.ofNullable(rule.dateXPath)
      .map { "&dateXPath=${encode(it)}" }.orElse("")
  }

  fun getArticlesByRule(
    corrId: String,
    rule: FeedRule,
    document: Document,
    url: URL,
    sampleSize: Int = 0
  ): List<ArticleJsonDto> {

    val now = Date()
    val sireUrl = url.toString()
    val locale = extractLocale(document, propertyService.locale)
    log.debug("[${corrId}] apply rule context=${rule.contextXPath} link=${rule.linkXPath}")
    return evaluateXPath(rule.contextXPath, document).mapNotNull { element ->
      try {
        val content = applyExtendElement(rule.extendContext, element)
        val link = evaluateXPath(rule.linkXPath, element).first()
        val pubDate =
          Optional.ofNullable(rule.dateXPath).map { dateXPath -> extractPubDate(corrId, dateXPath, element, locale)!! }
            .orElse(now)
        val linkText = link.text()
        val articleUrl = toAbsoluteUrl(url, link.attr("href"))

        val article = ArticleJsonDto(
          id = FeedUtil.toURI(articleUrl, sireUrl, now),
          title = linkText.replace(reLinebreaks, " "),
          url = articleUrl,
          content_text = webToTextTransformer.extractText(content),
          content_raw = withAbsUrls(content, url).selectFirst("div")!!.outerHtml(),
          content_raw_mime = "text/html",
          date_published = pubDate,
          main_image_url = null
        )

        if (qualifiesAsArticle(element, rule)) {
          article
        } else {
          null
        }
      } catch (e: Exception) {
        log.debug(e.message)
        null
      }
    }.filterIndexed { index, _ -> sampleSize == 0 || index < sampleSize }
  }

  private fun extractLocale(document: Document, fallback: Locale): Locale {
    val langStr = document.select("html[@lang]").attr("lang")
    return Optional.ofNullable(StringUtils.trimToNull(langStr))
      .map {
        run {
          log.info("Found lang ${it}")
          Locale.forLanguageTag(it)
        }
      }
      .orElse(fallback)
  }

  private fun extractPubDate(corrId: String, dateXPath: String, element: Element, locale: Locale): Date? {
    return runCatching {
      val timeElement = evaluateXPath(dateXPath, element).first()
      if (timeElement.hasAttr("datetime")) {
        parseDateFromTimeElement(corrId, timeElement.attr("datetime"), locale)
      } else {
        parseDateFromTimeElement(corrId, timeElement.text(), locale)
      }
    }.getOrNull()
  }

  fun parseDateFromTimeElement(corrId: String, dateTimeStrParam: String, localeParam: Locale?): Date? {
    log.debug("[${corrId}] parsing $dateTimeStrParam")
    val locale = Optional.ofNullable(localeParam).orElse(propertyService.locale)
    val dateTimeStr = dateTimeStrParam
      .trim()

    runCatching {
      val date = toDate(LocalDateTime.parse(dateTimeStr))
      log.debug("[${corrId}] -> $date")
      return date
    }

    runCatching {
      val date = toDate(LocalDate.parse(dateTimeStr).atTime(8, 0))
      log.debug("[${corrId}] -> $date")
      return date
    }

    return runCatching {
      val simpleDateTimeStr = dateTimeStr
        .replace("[^a-z0-9: ]".toRegex(RegexOption.IGNORE_CASE), "")
        .replace("\\s+".toRegex(), " ")
      val (format, hasTime) = guessDateFormat(simpleDateTimeStr)!!
      val formatter = DateTimeFormatter.ofPattern(format, locale)
      val date = if (hasTime) {
        toDate(LocalDateTime.parse(simpleDateTimeStr, formatter))
      } else {
        toDate(LocalDate.parse(simpleDateTimeStr, formatter).atTime(8, 0))
      }
      log.debug("[${corrId}] -> $date")
      date
    }.onFailure { log.error("Cannot parse dateString $dateTimeStr") }.getOrNull()
  }

  private fun toDate(dt: LocalDateTime): Date {
    return Date.from(dt.atZone(ZoneId.systemDefault()).toInstant())
  }


  // credits https://stackoverflow.com/a/3390252
  /**
   * Determine SimpleDateFormat pattern matching with the given date string. Returns null if
   * format is unknown. You can simply extend DateUtil with more formats if needed.
   * @param dateString The date string to determine the SimpleDateFormat pattern for.
   * @return The matching SimpleDateFormat pattern, or null if format is unknown.
   * @see SimpleDateFormat
   */
  private fun guessDateFormat(dateString: String): Pair<String, Boolean>? {
    return dateFormatToRegexp
      .filter { (regex, dateFormat, _) ->
        run {
          val matches = regex.matches(dateString)
          if (matches) {
            log.debug("$dateString looks like $dateFormat")
          }
          matches
        }
      }
      .map { (_, dateFormat, hasTime) -> Pair(dateFormat, hasTime) }
      .firstOrNull()
  }

  private fun applyExtendElement(extendContext: String, element: Element): Element {
    val p =
      if (extendContext.indexOf(ExtendContext.PREVIOUS.value) > -1) element.previousElementSibling()
        ?.outerHtml() else ""
    val n =
      if (extendContext.indexOf(ExtendContext.NEXT.value) > -1) element.nextElementSibling()?.outerHtml() else ""
    return Jsoup.parse("<div>${p}${element.outerHtml()}${n}</div>")
  }


  private fun getRelativeCssPath(nodeParam: Element, context: Element): String {
    if (nodeParam == context) {
      // todo mag this is not applicable
      return "self"
    }
    var node = nodeParam
    var path = node.tagName() // tagName for text nodes is undefined
    while (node.parentNode() !== context && node.hasParent()) {
      node = node.parent()!!
      path = "${getTagName(node)}>${path}"
    }
    return path
  }

  private fun getTagName(node: Element): String {
    val classList = node.attr("class").split(" ")
      .filter { cn -> cn.matches(reNumber) }
    if (classList.isNotEmpty()) {
      return "${node.tagName()}.${classList.joinToString(".")}"
    }
    return node.tagName()
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
    val xpathResult = Xsoup.compile(fixRelativePath(xpath)).evaluate(context).elements
    return xpathResult.toList()
  }

  private fun fixRelativePath(xpath: String): String {
    return if (xpath.startsWith("./")) {
      xpath.replaceFirst("./", "//")
    } else {
      xpath
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

  private fun scoreRule(rule: CandidateFeedRule): CandidateFeedRule {
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
    if (contextPathContains("header")) score -= 2
    if (contextPathContains("nav")) score--
    if (contextPathContains("article")) score += 2
    if (contextPathContains("main")) score += 2
    if (contextPathContains("aside")) score -= 2
    if (contextPathContains("footer")) score -= 2
    if (contextPathContains("ul>li")) score--
    if (linkPathContains("h1")) score += 4
    if (linkPathContains("h2")) score += 3
    if (linkPathContains("h3")) score += 2
    if (linkPathContains("h4")) score++
    if (linkPathContains("strong")) score++
    if (linkPathContains("aside")) score -= 2
    if (linkPathContains("article")) score += 2
    // if (rule.linkPath.toLowerCase() === "a") score --
    if (rule.contextXPath.lowercase(Locale.getDefault()).endsWith("a")) score -= 5
    if (rule.linkXPath.lowercase(Locale.getDefault()) === "self") score--

    // punish bad link texts
    val linkElements = rule.contexts.mapNotNull { context ->
      evaluateXPath(
        rule.linkXPath,
        context.contextElement
      ).firstOrNull()
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
    if (rule.contexts.size < 3) score--
    if (rule.contexts.size > 5) score++
    if (rule.contexts.size > 10) score++

    log.debug("rule ${rule.contextXPath} score -> $score")
    return CandidateFeedRule(
      count = rule.count,
      score = score,
      contexts = rule.contexts,
      linkXPath = rule.linkXPath,
      extendContext = rule.extendContext,
      contextXPath = rule.contextXPath,
      dateXPath = rule.dateXPath
    )
  }

  private fun findLinks(document: Document): List<LinkPointer> {
    val body = document.body()
    return document.select("A[href]").stream()
      .filter { element -> toWords(element.text()).size >= minWordCountOfLink }
      .map { element ->
        LinkPointer(
          element = element,
          index = getChildIndex(element),
          path = getRelativeCssPath(element, body)
        )
      }
      .collect(Collectors.toList())

  }

  private fun findArticleRootElement(linkElementsParam: List<Element>): List<Element> {
    var linkElements = linkElementsParam
    while (true) {
      if (linkElements.stream().anyMatch { currentNode -> !currentNode.hasParent() }) {
        break
      }
      val parentNodes = linkElements.map { currentNode -> currentNode.parent() }
      if (parentNodes.isEmpty()) {
        break
      }
      if (parentNodes[0] == parentNodes[1]) {
        break
      }
      linkElements = parentNodes.filterNotNull()
    }
    return linkElements
  }

  private fun findArticleContext(
    groupId: String,
    linkPointers: List<LinkPointer>,
  ): List<ArticleContext> {
    val linkElements = linkPointers.map { linkPointer -> linkPointer.element }
    val articleRootElements = findArticleRootElement(linkElements)

    return linkPointers.mapIndexed { index, linkPointer ->
      ArticleContext(
        id = groupId,
        linkElement = linkPointer.element,
        contextElement = articleRootElements[index],
        dateElement = null
      )
    }
  }

  private fun convertContextsToRule(contexts: List<ArticleContext>, root: Element): CandidateFeedRule {
    val linkXPath = "./" + __generalizeXPaths(contexts.map { context ->
      getRelativeXPath(
        context.linkElement,
        context.contextElement
      )
    }.toSet())
    val dateXPath = if (contexts.first().dateElement != null) {
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
    return CandidateFeedRule(
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
    element.select("a[href]").forEach { link ->
      link.attr("href", toAbsoluteUrl(url, link.attr("href")))
    }
    return element
  }
}
