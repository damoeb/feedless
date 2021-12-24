package org.migor.rss.rich.transform

import org.apache.commons.lang3.StringUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.safety.Safelist
import org.jsoup.select.NodeTraversor
import org.jsoup.select.NodeVisitor
import org.springframework.stereotype.Service
import java.util.*

@Service
class MarkupSimplifier {
  fun simplify(elementParam: Element?): String {
    return Optional.ofNullable(elementParam).map { context -> run {
      oneline(flatten(clean(compact(context.clone()))))
    }}.orElse("")
  }

  private fun oneline(element: Element): String {
    return "<article>${element.html().replace(Regex("[\n\r\t]+", RegexOption.MULTILINE), " ")
      .replace(Regex("[ ]{2,}", RegexOption.MULTILINE), " ")}</article>"
  }

  fun getSafelist(): Safelist {
    return Safelist.relaxed()
      .addTags("div", "section", "header", "footer", "figure", "picture", "figcaption")
      .addAttributes("img", "src")
      .addAttributes("a", "href")
  }

  private fun clean(context: Element): Element {
    return Jsoup.parse(Jsoup.clean(context.html(), getSafelist())).body()
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
        if (node is Element && node.tagName() == "div" && node.childrenSize() == 1 && node.child(0).tagName() == "div") {
          substitutions.add(Pair(node, node.child(0)))
        }
      }

      override fun tail(node: Node, depth: Int) {
      }
    }
  }
}
