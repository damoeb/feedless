package org.migor.rich.rss.web

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import org.springframework.stereotype.Service
import java.util.*

@Service
class WebToTextTransformer {
  fun extractText(elementParam: Element?): String {
    return Optional.ofNullable(elementParam).map { context ->
      run {

        val builder = StringBuilder()
        NodeTraversor.traverse(tagNormalizer(builder), context)

        builder.toString().trimEnd()
      }
    }.orElse("")
  }

  private fun tagNormalizer(builder: StringBuilder): NodeVisitor {
    return object : NodeVisitor {
      override fun head(node: Node, depth: Int) {
        if (node is TextNode) {
          builder.append(node.text())
        }
      }

      private fun isInlineTag(node: Element): Boolean {
        return when (node.tagName()) {
          "em", "strong", "i", "b", "mark", "small", "del", "ins", "sub", "sup", "cite", "var", "a" -> true
          else -> false
        }
      }

      override fun tail(node: Node, depth: Int) {
        if (node is Element) {
          if (!isInlineTag(node)) {
            builder.append("\n")
          }
        }
      }
    }
  }
}
