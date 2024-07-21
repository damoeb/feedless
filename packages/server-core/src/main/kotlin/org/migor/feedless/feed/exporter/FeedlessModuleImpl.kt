package org.migor.feedless.feed.exporter

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.rometools.rome.feed.CopyFrom
import com.rometools.rome.feed.impl.CloneableBean
import com.rometools.rome.feed.impl.CopyFromHelper
import com.rometools.rome.feed.module.ModuleImpl
import java.util.*

class FeedlessModuleImpl: ModuleImpl(FeedlessModule::class.java, URI), FeedlessModule {
  @Transient
  @Expose(serialize = false, deserialize = false)
  private var COPY_FROM_HELPER: CopyFromHelper

  @SerializedName(STARTING_AT)
  private var startingAt: Date? = null

  @SerializedName(LAT_LNG)
  private var latLng: String? = null

  init {
    val basePropInterfaceMap: MutableMap<String, Class<*>> = HashMap<String, Class<*>>()
    basePropInterfaceMap["startingAt"] = Date::class.java
    val basePropClassImplMap: Map<Class<out CopyFrom>, Class<*>> = HashMap<Class<out CopyFrom>, Class<*>>()

    COPY_FROM_HELPER = CopyFromHelper(FeedlessModule::class.java, basePropInterfaceMap, basePropClassImplMap)
  }

  companion object {
    const val URI: String = "http://feedless.org/xml/1.0/"
    val NAMESPACE = org.jdom2.Namespace.getNamespace("feedless", URI)
    const val STARTING_AT = "startingAt"
    const val LAT_LNG = "latLon"
    const val LAT = "lat"
    const val LNG = "lon"
  }

  override fun getStartingAt(): Date? = startingAt

  override fun setStartingAt(date: Date?) {
    startingAt = date
  }

  override fun getLatLng(): String? = latLng

  override fun setLatLng(value: String?) {
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
