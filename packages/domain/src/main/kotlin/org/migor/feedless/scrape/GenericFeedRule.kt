package org.migor.feedless.scrape

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.jsoup.nodes.Element

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
