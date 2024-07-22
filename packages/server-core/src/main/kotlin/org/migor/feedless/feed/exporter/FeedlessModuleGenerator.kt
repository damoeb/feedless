package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.module.Module
import com.rometools.rome.io.ModuleGenerator
import com.rometools.rome.io.impl.DateParser
import org.jdom2.Element
import org.jdom2.Namespace
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.util.JsonUtil
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class FeedlessModuleGenerator: ModuleGenerator {

  override fun getNamespaceUri(): String {
    return FeedlessModuleImpl.URI
  }

  override fun getNamespaces(): Set<Namespace> {
    return setOf(FeedlessModuleImpl.NAMESPACE)
  }

  override fun generate(module: Module, element: Element) {
    var root = element
    while (root.parent != null && root.parent is Element) {
      root = element.parent as Element
    }
    root.addNamespaceDeclaration(FeedlessModuleImpl.NAMESPACE)

    val feedlessModule = module.castToFeedlessModule()

    val startingAt = feedlessModule.getStartingAt()
    if (startingAt != null) {
      element.addContent(generateElement(FeedlessModuleImpl.STARTING_AT, DateParser.formatW3CDateTime(startingAt, Locale.US)))
    }

    val page = feedlessModule.getPage()
    if (page != null) {
      element.addContent(generateElement(FeedlessModuleImpl.PAGE, page.toString()))
    }

    val latlngStr = feedlessModule.getLatLng()
    if (latlngStr != null) {
      val latlng = JsonUtil.gson.fromJson(latlngStr, JsonPoint::class.java)
      val latLngElement = Element(FeedlessModuleImpl.LAT_LNG, FeedlessModuleImpl.NAMESPACE)
      latLngElement.setAttribute(FeedlessModuleImpl.LAT, latlng.x.toString())
      latLngElement.setAttribute(FeedlessModuleImpl.LNG, latlng.y.toString())
      element.addContent(latLngElement)
    }
  }

  private fun generateElement(name: String, value: String): Element {
    val element = Element(name, FeedlessModuleImpl.NAMESPACE)
    element.addContent(value)
    return element
  }
}

fun Module.castToFeedlessModule(): FeedlessModuleImpl {
  val bout = ByteArrayOutputStream()
  val oout = ObjectOutputStream(bout)
  oout.writeObject(this)
  oout.close()

  val oin = ObjectInputStream(ByteArrayInputStream(bout.toByteArray()))
  return oin.readObject() as FeedlessModuleImpl
}
