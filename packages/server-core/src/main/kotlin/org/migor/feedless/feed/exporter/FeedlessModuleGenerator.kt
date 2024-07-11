package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.module.Module
import com.rometools.rome.io.ModuleGenerator
import com.rometools.rome.io.impl.DateParser
import org.jdom2.Element
import org.jdom2.Namespace
import org.migor.feedless.generated.types.GeoPoint
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

    val startingAtField = module.javaClass.getDeclaredField("startingAt")
    startingAtField.isAccessible = true
    val startingAt = startingAtField.get(module) as Date?

    if (startingAt != null) {
      element.addContent(generateElement(FeedlessModuleImpl.STARTING_AT, DateParser.formatW3CDateTime(startingAt, Locale.US)))
    }

    val latlngField = module.javaClass.getDeclaredField("latLng")
    latlngField.isAccessible = true
    val latlng = latlngField.get(module) as GeoPoint?

    if (latlng != null) {
      val latLngElement = Element(FeedlessModuleImpl.LAT_LNG, FeedlessModuleImpl.NAMESPACE)
      latLngElement.setAttribute(FeedlessModuleImpl.LAT, latlng.lat.toString())
      latLngElement.setAttribute(FeedlessModuleImpl.LNG, latlng.lon.toString())
      element.addContent(latLngElement)
    }
  }

  private fun generateElement(name: String, value: String): Element {
    val element = Element(name, FeedlessModuleImpl.NAMESPACE)
    element.addContent(value)
    return element
  }
}
