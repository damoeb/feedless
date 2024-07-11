package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.module.Module
import com.rometools.rome.io.ModuleParser
import com.rometools.rome.io.impl.DateParser
import org.jdom2.Element
import org.migor.feedless.generated.types.GeoPoint
import java.util.*


class FeedlessModuleParser: ModuleParser {
  override fun getNamespaceUri(): String {
    return FeedlessModuleImpl.URI
  }

  override fun parse(element: Element, locale: Locale?): Module? {
    var match = false

    val module: FeedlessModule = FeedlessModuleImpl()

    val dateElement = element.getChild(FeedlessModuleImpl.STARTING_AT, FeedlessModuleImpl.NAMESPACE)
    if (dateElement != null) {
      match = true
      module.setStartingAt(DateParser.parseW3CDateTime(dateElement.text, locale))
    }

    val latLngElement = element.getChild(FeedlessModuleImpl.LAT_LNG, FeedlessModuleImpl.NAMESPACE)
    if (latLngElement != null) {
      match = true
      module.setLatLng(GeoPoint(
        lat = latLngElement.getAttribute(FeedlessModuleImpl.LAT).doubleValue,
        lon = latLngElement.getAttribute(FeedlessModuleImpl.LNG).doubleValue,
      ))
    }

    return if (match) module else null
  }
}
