package org.migor.feedless.web

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import org.migor.feedless.util.HtmlUtil
import org.springframework.stereotype.Service
import java.util.*

@Service
class MarkupSimplifier {
  fun simplify(elementParam: Element?): String {
    return Optional.ofNullable(elementParam).map { context ->
      run {
        oneline(flatten(Jsoup.parse(HtmlUtil.cleanHtml(compact(context.clone()).html()))))
      }
    }.orElse("")
  }

  private fun oneline(element: Element): String {
    return "<article>${
      element.html().replace(Regex("[\n\r\t]+", RegexOption.MULTILINE), " ")
        .replace(Regex("[ ]{2,}", RegexOption.MULTILINE), " ")
    }</article>"
  }

  /**
   * Removes empty nodes
   */
  private fun compact(context: Element): Element {
    val toRemoveStack = Stack<Element>()
    NodeTraversor.traverse(emptyElements(toRemoveStack), context)
    while (toRemoveStack.isNotEmpty()) {
      val toRemove = toRemoveStack.pop()
      if (toRemove.hasParent()) {
        val parent = toRemove.parent()!!
        toRemove.remove()
        if (!(parent.childrenSize() > 0 || hasOwnText(parent))) {
          toRemoveStack.add(parent)
        }
      } else {
        toRemove.remove()
      }
    }
    return context
  }

  /**
   * Flattens nested divs
   */
  private fun flatten(element: Element): Element {
    val substitutions = mutableListOf<Pair<Element, Element>>()
    NodeTraversor.traverse(removeNested(substitutions), element)
    substitutions.forEach { (from, to) -> from.replaceWith(to) }
    return element
  }

  private fun isEmptyNode(node: Element): Boolean {
    return node.tagName() != "img" && node.childrenSize() == 0 && !hasOwnText(node)
  }

  private fun hasOwnText(node: Element): Boolean {
    return StringUtils.isNotBlank(node.ownText())
  }

  private fun emptyElements(toRemove: Stack<Element>): NodeVisitor {
    return object : NodeVisitor {
      override fun head(node: Node, depth: Int) {
        if (node is Element && isEmptyNode(node)) {
          toRemove.add(node)
        }
      }

      override fun tail(node: Node, depth: Int) {
      }
    }
  }

  private fun removeNested(substitutions: MutableList<Pair<Element, Element>>): NodeVisitor {
    return object : NodeVisitor {
      override fun head(node: Node, depth: Int) {
        if (node is Element && node.tagName() == "div" && node.childrenSize() == 1 && node.child(0)
            .tagName() == "div"
        ) {
          substitutions.add(Pair(node, node.child(0)))
        }
      }

      override fun tail(node: Node, depth: Int) {
      }
    }
  }
}
