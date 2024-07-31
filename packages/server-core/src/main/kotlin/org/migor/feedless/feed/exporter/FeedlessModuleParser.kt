package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.module.Module
import com.rometools.rome.io.ModuleParser
import com.rometools.rome.io.impl.DateParser
import org.jdom2.Element
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.util.JsonUtil
import java.util.*


class FeedlessModuleParser: ModuleParser {
  override fun getNamespaceUri(): String {
    return FeedlessModuleImpl.URI
  }

  override fun parse(element: Element, locale: Locale?): Module? {
    var match = false

    val module: FeedlessModule = FeedlessModuleImpl()

    val dateElement = element.getChild(FeedlessModuleImpl.ELEMENT_STARTING_AT, FeedlessModuleImpl.NAMESPACE)
    if (dateElement != null) {
      match = true
      module.setStartingAt(DateParser.parseW3CDateTime(dateElement.text, locale))
    }

    val latLngElement = element.getChild(FeedlessModuleImpl.ELEMENT_LAT_LNG, FeedlessModuleImpl.NAMESPACE)
    if (latLngElement != null) {
      match = true
      val point = JsonPoint()
      point.x = latLngElement.getAttribute(FeedlessModuleImpl.ATTR_LAT).doubleValue
      point.y = latLngElement.getAttribute(FeedlessModuleImpl.ATTR_LNG).doubleValue
      module.setLatLng(JsonUtil.gson.toJson(point))
    }

    val dataElement = element.getChild(FeedlessModuleImpl.ELEMENT_DATA, FeedlessModuleImpl.NAMESPACE)
    if (dataElement != null) {
      match = true
      module.setData(dataElement.text)
      module.setDataType(dataElement.getAttribute(FeedlessModuleImpl.ATTR_DATA_TYPE).value)
    }

    val pageElement = element.getChild(FeedlessModuleImpl.ELEMENT_PAGE, FeedlessModuleImpl.NAMESPACE)
    if (pageElement != null) {
      match = true
      module.setPage(pageElement.value.toInt())
    }

    return if (match) module else null
  }
}
