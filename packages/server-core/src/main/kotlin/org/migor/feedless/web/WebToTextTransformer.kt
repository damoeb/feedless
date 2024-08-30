package org.migor.feedless.web

import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import org.springframework.stereotype.Service

@Service
class WebToTextTransformer {
  suspend fun extractText(elementParam: Element?): String {
    return elementParam?.let {
      val builder = StringBuilder()
      NodeTraversor.traverse(tagNormalizer(builder), it)

      builder.toString().trimEnd()
    } ?: ""
  }

  private suspend fun tagNormalizer(builder: StringBuilder): NodeVisitor {
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
