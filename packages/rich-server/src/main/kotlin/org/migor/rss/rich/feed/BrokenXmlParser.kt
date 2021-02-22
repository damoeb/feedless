package org.migor.rss.rich.feed

import com.guseyn.broken_xml.Attribute
import com.guseyn.broken_xml.Element
import com.guseyn.broken_xml.ParsedXML

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
    return """<${element.name()} ${serializeAttributes(element.attributes())}>
  ${element.texts()}
  ${serializeElements(element.children())}
</${element.name()}>"""
  }


}
