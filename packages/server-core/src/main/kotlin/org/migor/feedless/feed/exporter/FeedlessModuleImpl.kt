package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.CopyFrom
import com.rometools.rome.feed.impl.CloneableBean
import com.rometools.rome.feed.impl.CopyFromHelper
import com.rometools.rome.feed.module.ModuleImpl
import org.migor.feedless.generated.types.GeoPoint
import java.util.*

class FeedlessModuleImpl: ModuleImpl(FeedlessModule::class.java, URI), FeedlessModule {
  private var COPY_FROM_HELPER: CopyFromHelper
  private var startingAt: Date? = null
  private var latLng: GeoPoint? = null

  init {
    val basePropInterfaceMap: MutableMap<String, Class<*>> = HashMap<String, Class<*>>()
    basePropInterfaceMap["startingAt"] = Date::class.java
    val basePropClassImplMap: Map<Class<out CopyFrom>, Class<*>> = HashMap<Class<out CopyFrom>, Class<*>>()

    COPY_FROM_HELPER = CopyFromHelper(FeedlessModule::class.java, basePropInterfaceMap, basePropClassImplMap)
  }

  companion object {
    val URI: String = "http://feedless.org/xml/1.0/"
    val NAMESPACE = org.jdom2.Namespace.getNamespace("feedless", URI)
    val STARTING_AT = "startingAt"
    val LAT_LNG = "latLon"
    val LAT = "lat"
    val LNG = "lon"

  }

  override fun getStartingAt(): Date? = startingAt

  override fun setStartingAt(date: Date?) {
    startingAt = date
  }

  override fun getLatLng(): GeoPoint? = latLng

  override fun setLatLng(value: GeoPoint?) {
    latLng = value
  }

  override fun clone(): Any {
    return CloneableBean.beanClone(this, emptySet())
  }

  override fun getInterface(): Class<out CopyFrom> = FeedlessModule::class.java

  override fun copyFrom(obj: CopyFrom?) {
    COPY_FROM_HELPER.copy(this, obj)
  }

  override fun getUri(): String = URI
}
