package org.migor.feedless.feed.exporter

import com.google.gson.Gson
import com.rometools.rome.feed.module.Module
import com.rometools.rome.io.ModuleGenerator
import com.rometools.rome.io.impl.DateParser
import org.jdom2.Element
import org.jdom2.Namespace
import org.migor.feedless.feed.parser.json.JsonPoint
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

class FeedlessModuleGenerator : ModuleGenerator {

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
      element.addContent(
        generateElement(
          FeedlessModuleImpl.ELEMENT_STARTING_AT,
          DateParser.formatW3CDateTime(startingAt, Locale.US)
        )
      )
    }

    val page = feedlessModule.getPage()
    if (page != null) {
      element.addContent(generateElement(FeedlessModuleImpl.ELEMENT_PAGE, page.toString()))
    }

    val latlngStr = feedlessModule.getLatLng()
    if (latlngStr != null) {
      val latlng = Gson().fromJson(latlngStr, JsonPoint::class.java)
      val latLngElement = Element(FeedlessModuleImpl.ELEMENT_LAT_LNG, FeedlessModuleImpl.NAMESPACE)
      latLngElement.setAttribute(FeedlessModuleImpl.ATTR_LAT, latlng.x.toString())
      latLngElement.setAttribute(FeedlessModuleImpl.ATTR_LNG, latlng.y.toString())
      element.addContent(latLngElement)
    }

    val data = feedlessModule.getData()
    if (data != null) {
      val dataElement = Element(FeedlessModuleImpl.ELEMENT_DATA, FeedlessModuleImpl.NAMESPACE)
      dataElement.setAttribute(FeedlessModuleImpl.ATTR_DATA_TYPE, feedlessModule.getDataType())
      dataElement.text = data
      element.addContent(dataElement)
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
