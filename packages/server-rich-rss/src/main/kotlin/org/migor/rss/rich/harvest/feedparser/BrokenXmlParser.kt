package org.migor.rss.rich.harvest.feedparser

import com.guseyn.broken_xml.Attribute
import com.guseyn.broken_xml.Element
import com.guseyn.broken_xml.ParsedXML
import com.guseyn.broken_xml.Text
import org.apache.commons.lang3.StringUtils

object BrokenXmlParser {

  fun parse(responseBody: String): String {
    val document = ParsedXML(responseBody).document()
    return serializeElement(document.roots().first())
  }

  private fun serializeElements(elements: List<Element>): String {
    return elements.joinToString(separator = "\n") { child: Element -> serializeElement(child) }
  }

  private fun serializeAttributes(attributes: List<Attribute>): String {
    return attributes.joinToString(separator = " ") { attribute: Attribute -> "${attribute.name()}=\"${attribute.value()}\"" }
  }

  private fun serializeElement(element: Element): String {
    val texts = StringUtils.trimToEmpty(element.texts().joinToString(separator = " ") { text: Text -> text.value() })
    val attributes = serializeAttributes(element.attributes())
    val tag = element.name()
    return if (element.children().isEmpty()) {
      """<$tag $attributes>$texts</$tag>"""
    } else {
      val elements = serializeElements(element.children())
      """<$tag $attributes>
  $texts
  $elements
</$tag>"""
    }
  }


}
