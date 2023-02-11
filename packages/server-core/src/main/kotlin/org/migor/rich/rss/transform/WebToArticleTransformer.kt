package org.migor.rich.rss.transform

import org.apache.commons.lang3.StringUtils
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.migor.rich.rss.util.HtmlUtil
import org.migor.rich.rss.util.HtmlUtil.parseHtml
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.URL
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToInt

/*
 *  Copyright 2011 Peter Karich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Forked from https://github.com/mohaps/xtractor
 *
 * Modifications: rewrite to kotlin, refactoring, article markup with simplification
 *
 * @author Peter Karich
 * @author damoeb
 */
@Service
class WebToArticleTransformer(
  @Autowired
  private var markupSimplifier: MarkupSimplifier
) {

  private val log = LoggerFactory.getLogger(WebToArticleTransformer::class.simpleName)


  // Unlikely candidates
  private val UNLIKELY = Pattern.compile(
    "com(bx|ment|munity)|dis(qus|cuss)|e(xtra|[-]?mail)|foot|"
      + "header|menu|re(mark|ply)|rss|sh(are|outbox)|sponsor"
      + "a(d|ll|gegate|rchive|ttachment)|(pag(er|ination))|popup|print|"
      + "login|si(debar|gn|ngle)"
  )

  // Most likely positive candidates
  private val POSITIVE = Pattern.compile(
    "(^(body|content|h?entry|main|page|post|text|blog|story|haupt))"
      + "|arti(cle|kel)|instapaper_body"
  )

  // Most likely negative candidates
  private val NEGATIVE = Pattern.compile(
    "nav($|igation)|user|com(ment|bx)|(^com-)|contact|"
      + "foot|masthead|(me(dia|ta))|outbrain|promo|related|scroll|(sho(utbox|pping))|"
      + "sidebar|sponsor|tags|tool|widget|player|disclaimer|toc|infobox|vcard"
  )

  private val NODES = Pattern
    .compile("p|div|td|h1|h2|article|section")
  private val NEGATIVE_STYLE = Pattern
    .compile("hidden|display: ?none|font-size: ?small")
  private val IGNORED_TITLE_PARTS = setOf("hacker news", "facebook")

  fun fromHtml(html: String, url: String): ExtractedArticle {
    return fromDocument(parseHtml(html, url), url)
  }

  fun fromDocument(doc: Document, url: String): ExtractedArticle {
    doc.select("script").remove()
    doc.select("style").remove()
    doc.select("[href]").forEach { a -> a.attr("href", a.absUrl("href")) }
    val extracted = ExtractedArticle(url)
    extracted.contentMime = "text/html"
    return extractContent(extracted, doc)
  }

  private fun extractContent(res: ExtractedArticle, doc: Document): ExtractedArticle {
    res.title = extractTitle(doc)

    // now remove the clutter
    prepareDocument(doc)

    // init elements
    val nodes = getNodes(doc)
    var maxWeight = 0
    var bestMatchElement: Element? = null
    for (entry in nodes) {
      val currentWeight = getWeight(entry)
      if (currentWeight > maxWeight) {
        maxWeight = currentWeight
        bestMatchElement = entry
        if (maxWeight > 200) break
      }
    }

    // mohaps: this promotes the parent of best match to the best match element
    // if enough siblings of current best match have close childweight scores
    // fix was needed for url :
    bestMatchElement?.let {
      val bestParent = it.parent()
      bestParent?.let {
        val childWeightStr = bestParent.attr("childweight")
        var childWeight = 0
        if (childWeightStr.isNotEmpty()) {
          childWeight = childWeightStr.toInt()
        }
        val childNodes = bestParent.childNodes()
        var low = childWeight - 10
        if (low < 0) {
          low = 0
        }
        val high = childWeight + 30
        var siblingScore = 0.0
        for (childNode in childNodes) {
          if (childNode is Element) {
            val childElement = childNode
            if (childElement !== bestMatchElement) {
              val cWeight = childElement.attr("childweight")
              var thisChildWeight = 0
              if (cWeight.trim { it <= ' ' }.length > 0) {
                thisChildWeight = childElement
                  .attr("childweight").toInt()
              }
              if (thisChildWeight > low) {
                siblingScore += 1.0
                if (thisChildWeight <= high) {
                  siblingScore += 1.0
                }
              }
            }
            if (siblingScore >= 3.0) {
              bestMatchElement = bestParent
              break
            }
          }
        }
      }
    }
    bestMatchElement?.let { bestMatch ->
      var imgEl = determineImageSource(bestMatch)
      if (imgEl != null) {
        res.imageUrl = SHelper.replaceSpaces(imgEl.attr("src"))
        // TODO remove parent container of image if it is contained in
        // bestMatchElement
        // to avoid image subtitles flooding in
      } else {
        imgEl = determineImageSource(doc.body())
        imgEl?.let {
          res.imageUrl = SHelper.replaceSpaces(imgEl.attr("src"))
        }
      }

      // clean before grabbing text
      val text = bestMatch.text()
      // this fails for short facebook post and probably tweets:
      if (text.length > res.title!!.length) {
        res.contentText = text
      }
    }
    if (StringUtils.isNotBlank(res.imageUrl)) {
      res.imageUrl = extractImageUrl(doc)
    }

    res.content = markupSimplifier.simplify(bestMatchElement)
    res.faviconUrl = extractFaviconUrl(doc)

    // mohaps: hack to get absolute url of image
    var imgUrl = res.imageUrl
    if (imgUrl == null || imgUrl.isEmpty()) {
      return res
    }
    if (!(imgUrl.startsWith("http://") || imgUrl.startsWith("https://"))) {
      if (imgUrl.startsWith("/")) {
        if (!imgUrl.startsWith("//")) {
          val rootUrl = SHelper.extractDomain(
            res.originalUrl,
            false
          )
          imgUrl = rootUrl + imgUrl
        }
      } else if (!imgUrl.startsWith("./") || !imgUrl.startsWith("../")) {
        val originalUrl = res.originalUrl
        if (originalUrl.endsWith("/")) {
          imgUrl = originalUrl + imgUrl
        } else {
          imgUrl = imgUrl.split("\\?").toTypedArray()[0]
          val index = originalUrl.lastIndexOf('/', 8)
          imgUrl = if (index >= 0) {
            originalUrl.substring(0, index) + "/" + imgUrl
          } else {
            "$originalUrl/$imgUrl"
          }
        }
      }
    }
    if (!imgUrl.startsWith("http")) {
      imgUrl = (URL(res.originalUrl).protocol.toString()
        + "://" + imgUrl)
    }
    res.imageUrl = SHelper.getLargestPossibleImageUrl(imgUrl)
    return res
  }

  private fun extractTitle(doc: Document): String {
    var title = cleanTitle(doc.title())
    if (title.isEmpty()) {
      title = StringUtils.trim(doc.select("head title").text())
      if (title.isEmpty()) {
        title = StringUtils.trim(
          doc.select("head meta[name=title]")
            .attr("content")
        )
        if (title.isEmpty()) {
          title = StringUtils.trim(
            doc.select(
              "head meta[property=og:title]"
            ).attr("content")
          )
          if (title.isEmpty()) {
            title = StringUtils.trim(
              doc.select(
                "head meta[name=twitter:title]"
              )
                .attr("content")
            )
          }
        }
      }
    }
    return title
  }

  /**
   * Tries to extract an image url from metadata if determineImageSource
   * failed
   *
   * @return image url or empty str
   */
  private fun extractImageUrl(doc: Document): String {
    // use open graph tag to get image
    var imageUrl = SHelper.replaceSpaces(
      doc.select(
        "head meta[property=og:image]"
      ).attr("content")
    )
    if (imageUrl.isEmpty()) {
      imageUrl = SHelper.replaceSpaces(
        doc.select(
          "head meta[name=twitter:image]"
        ).attr("content")
      )
      if (imageUrl.isEmpty()) {
        // prefer link over thumbnail-meta if empty
        imageUrl = SHelper.replaceSpaces(
          doc.select(
            "link[rel=image_src]"
          ).attr("href")
        )
        if (imageUrl.isEmpty()) {
          imageUrl = SHelper.replaceSpaces(
            doc.select(
              "head meta[name=thumbnail]"
            ).attr("content")
          )
        }
      }
    }
    return imageUrl
  }

  private fun extractFaviconUrl(doc: Document): String {
    var faviconUrl = SHelper.replaceSpaces(
      doc.select(
        "head link[rel=icon]"
      ).attr("href")
    )
    if (faviconUrl.isEmpty()) {
      faviconUrl = SHelper.replaceSpaces(
        doc.select(
          "head link[rel^=shortcut],link[rel$=icon]"
        ).attr("href")
      )
    }
    return faviconUrl
  }

  /**
   * Weights current element. By matching it with positive candidates and
   * weighting child nodes. Since it's impossible to predict which exactly
   * names, ids or class names will be used in HTML, major role is played by
   * child nodes
   *
   * @param e
   * Element to weight, along with child nodes
   */
  private fun getWeight(e: Element): Int {
    var weight = calcWeight(e)
    weight += (e.ownText().length / 100.0 * 10).roundToInt()
    val childWeight = weightChildNodes(e)
    weight += childWeight
    e.attr("childWeight", childWeight.toString())
    return weight
  }

  /**
   * Weights a child nodes of given Element. During tests some difficulties
   * were met. For instanance, not every single document has nested paragraph
   * tags inside of the major article tag. Sometimes people are adding one
   * more nesting level. So, we're adding 4 points for every 100 symbols
   * contained in tag nested inside of the current weighted element, but only
   * 3 points for every element that's nested 2 levels deep. This way we give
   * more chances to extract the element that has less nested levels,
   * increasing probability of the correct extraction.
   *
   * @param rootEl
   * Element, who's child nodes will be weighted
   */
  private fun weightChildNodes(rootEl: Element): Int {
    var weight = 0
    var caption: Element? = null
    val pEls: MutableList<Element> = ArrayList(5)
    for (child in rootEl.children()) {
      val ownText = child.ownText()
      val ownTextLength = ownText.length
      if (ownTextLength < 20) continue
      if (ownTextLength > 200) weight += Math.max(50, ownTextLength / 10)
      if (child.tagName() == "h1" || child.tagName() == "h2") {
        weight += 30
      } else if (child.tagName() == "div" || child.tagName() == "p") {
        weight += calcWeightForChild(child, ownText)
        if (child.tagName() == "p" && ownTextLength > 50) pEls.add(child)
        if (child.className().lowercase(Locale.getDefault()) == "caption") caption = child
      }
    }

    // use caption and image
    caption?.let { weight += 30 }
    if (pEls.size >= 2) {
      for (subEl in rootEl.children()) {
        if ("h1;h2;h3;h4;h5;h6".contains(subEl.tagName())) {
          weight += 20
        } else if ("table;li;td;th".contains(subEl.tagName())) {
          addScore(subEl, -30)
        }
        if ("p".contains(subEl.tagName())) addScore(subEl, 30)
      }
    }
    return weight
  }

  private fun addScore(el: Element, score: Int) {
    val old = getScore(el)
    setScore(el, score + old)
  }

  private fun getScore(el: Element): Int {
    var old = 0
    try {
      old = el.attr("gravityScore").toInt()
    } catch (ex: Exception) {
    }
    return old
  }

  private fun setScore(el: Element, score: Int) {
    el.attr("gravityScore", score.toString())
  }

  private fun calcWeightForChild(child: Element, ownText: String): Int {
    var c = SHelper.count(ownText, "&quot;")
    c += SHelper.count(ownText, "&lt;")
    c += SHelper.count(ownText, "&gt;")
    c += SHelper.count(ownText, "px")
    val `val`: Int
    `val` = if (c > 5) -30 else Math.round(ownText.length / 25.0).toInt()
    addScore(child, `val`)
    return `val`
  }

  private fun calcWeight(e: Element): Int {
    var weight = 0
    if (POSITIVE.matcher(e.className()).find()) weight += 35
    if (POSITIVE.matcher(e.id()).find()) weight += 40
    if (UNLIKELY.matcher(e.className()).find()) weight -= 20
    if (UNLIKELY.matcher(e.id()).find()) weight -= 20
    if (NEGATIVE.matcher(e.className()).find()) weight -= 50
    if (NEGATIVE.matcher(e.id()).find()) weight -= 50
    val style = e.attr("style")
    if (style.isNotEmpty() && NEGATIVE_STYLE.matcher(style).find()
    ) weight -= 50
    return weight
  }

  private fun determineImageSource(el: Element): Element? {
    var maxWeight = 0
    var maxNode: Element? = null
    var els = el.select("img")
    if (els.isEmpty()) {
      el.parent()?.let { parent ->
        els = parent.select("img")
      }
      if (els.isEmpty()) {
        el.parent()?.parent()?.let { parent ->
          els = parent.select("img")
        }
      }
    }
    var score = 1.0
    var firstIter = true
    for (e in els) {
      var sourceUrl = e.attr("src")
      if (sourceUrl.isEmpty()) {
        sourceUrl = e.attr("data-src")
      }
      if (sourceUrl.isEmpty()) {
        continue
      }
      if (sourceUrl.endsWith(".gif") || sourceUrl.contains(".gif")) {
        continue
      }
      if (sourceUrl.isEmpty() || isAdImage(sourceUrl) || sourceUrl.endsWith("spacer.gif") || sourceUrl.endsWith("PinExt.png")) {
        continue
      }
      if (sourceUrl.indexOf("/widget") > 0 || sourceUrl.indexOf("/icon") > 0) {
        continue
      }
      if (firstIter) {
        maxNode = e
        firstIter = false
      }
      var weight = 0
      val imgClass = e.attr("class")
      if (imgClass.isNotEmpty()) {
        if (imgClass.startsWith("main")) {
          maxNode = e
          break
        }
      }
      if (sourceUrl.indexOf("gravatar.com/") > 0) {
        weight -= 60
      }
      var heightNotFound = true
      var widthNotFound = true
      try {
        var notFound = true
        val s = e.attr("height")
        var height = 0
        if (s.length > 0) {
          height = s.toInt()
          notFound = false
        } else {
          height = 0
          if (height > 0) {
            notFound = false
          }
        }
        heightNotFound = notFound
        if (height > 100) weight += 20 else if (height < 100 && !notFound) weight -= 20 else if (height < 200 && !notFound) weight -= 10
      } catch (ex: Exception) {
      }
      try {
        var notFound = true
        val s = e.attr("width")
        var width = 0
        if (s.length > 0) {
          width = s.toInt()
          notFound = false
        } else {
          width = 0
          if (width > 0) {
            notFound = false
          }
        }
        widthNotFound = notFound
        if (width > 300) {
          weight += 200
        } else if (width > 100) {
          weight += 80
        } else if (width < 50 && !notFound) {
          weight += 50
        } else if (width < 100 && !notFound) {
          weight -= 40
        } else if (width < 200 && !notFound) {
          weight -= 30
        } else if (width < 300 && !notFound) {
          weight -= 20
        }
      } catch (ex: Exception) {
      }

      val alt = e.attr("alt")
      if (alt.length > 55) weight += 50 else if (alt.length > 35) weight += 20
      val title = e.attr("title")
      if (title.length > 55) weight += 50 else if (title.length > 35) weight += 20
      e.parent()?.let { parent ->
        val rel = parent.attr("rel")
        if (rel.contains("nofollow")) weight -= 40
      }
      weight = (weight * score).toInt()

      if (weight > maxWeight) {
        maxWeight = weight
        maxNode = e
        score /= 2
      }
    }
    return maxNode
  }

  /**
   * Prepares document. Currently only stipping unlikely candidates, since
   * from time to time they're getting more score than good ones especially in
   * cases when major text is short.
   *
   * @param doc
   * document to prepare. Passed as reference, and changed inside
   * of function
   */
  private fun prepareDocument(doc: Document) {
    doc.getElementsByTag("script").forEach { it.remove() }
    doc.getElementsByTag("style").forEach { it.remove() }
    doc.getElementsByTag("noscript").forEach { it.remove() }
  }

  private fun isAdImage(imageUrl: String): Boolean {
    return (imageUrl.endsWith(".gif")
      || SHelper.count(
      imageUrl,
      "ad"
    ) >= 2 || imageUrl.indexOf("scorecardresearch.com") > 0 || imageUrl.indexOf("doubleclick.com") > 0 || imageUrl.endsWith(
      "/logo.jpg"
    )
      || imageUrl.endsWith("/logo.png"))
  }

  /**
   * @return a set of all important nodes
   */
  private fun getNodes(doc: Document): Collection<Element> {
    val nodes: MutableMap<Element, Any?> = LinkedHashMap(64)
    var score = 100
    for (el in doc.select("body").select("*")) {
      if (NODES.matcher(el.tagName()).matches()) {
        nodes[el] = null
        setScore(el, score)
        score /= 2
      }
    }
    return nodes.keys
  }

  private fun cleanTitle(title: String): String {
    val res = StringBuilder()
    // int index = title.lastIndexOf("|");
    // if (index > 0 && title.length() / 2 < index)
    // title = title.substring(0, index + 1);
    var counter = 0
    val strs = title.split("\\|").toTypedArray()
    for (part in strs) {
      if (IGNORED_TITLE_PARTS.contains(part.lowercase(Locale.getDefault()).trim { it <= ' ' })) continue
      if (counter == strs.size - 1 && res.length > part.length) continue
      if (counter > 0) res.append("|")
      res.append(part)
      counter++
    }
    return StringUtils.trim(res.toString())
  }
}
