package org.migor.feedless.pipeline.plugins

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

fun Document.images(): List<Element> {
  return body().select("img[src]")
    .filter { imageElement -> imageElement.attr("src").startsWith("http") }
}
