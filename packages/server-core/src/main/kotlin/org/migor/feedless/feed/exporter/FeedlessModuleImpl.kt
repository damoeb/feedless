package org.migor.feedless.feed.exporter

import com.google.gson.annotations.Expose
import com.rometools.rome.feed.CopyFrom
import com.rometools.rome.feed.impl.CloneableBean
import com.rometools.rome.feed.impl.CopyFromHelper
import com.rometools.rome.feed.module.ModuleImpl
import java.util.*

class FeedlessModuleImpl: ModuleImpl(FeedlessModule::class.java, URI), FeedlessModule {
  @Transient
  @Expose(serialize = false, deserialize = false)
  private var COPY_FROM_HELPER: CopyFromHelper

  private var startingAt: Date? = null

  private var latLng: String? = null
  private var data: String? = null
  private var dataType: String? = null

  private var page: Int? = null

  init {
    val basePropInterfaceMap: MutableMap<String, Class<*>> = HashMap<String, Class<*>>()
    basePropInterfaceMap["startingAt"] = Date::class.java
    basePropInterfaceMap["latLng"] = String::class.java
    basePropInterfaceMap["page"] = Int::class.java
    val basePropClassImplMap: Map<Class<out CopyFrom>, Class<*>> = HashMap<Class<out CopyFrom>, Class<*>>()

    COPY_FROM_HELPER = CopyFromHelper(FeedlessModule::class.java, basePropInterfaceMap, basePropClassImplMap)
  }

  companion object {
    const val URI: String = "http://feedless.org/xml/1.0"
    val NAMESPACE = org.jdom2.Namespace.getNamespace("feedless", URI)
    const val ELEMENT_STARTING_AT = "startingAt"
    const val ELEMENT_PAGE = "page"
    const val ELEMENT_LAT_LNG = "latLon"
    const val ELEMENT_DATA = "data"
    const val ATTR_DATA_TYPE = "type"
    const val ATTR_LAT = "lat"
    const val ATTR_LNG = "lon"
  }

  override fun getStartingAt(): Date? = startingAt

  override fun setStartingAt(date: Date?) {
    startingAt = date
  }

  override fun getLatLng(): String? = latLng
  override fun setData(data: String?) {
    this.data = data
  }

  override fun getData(): String? = data

  override fun setDataType(type: String?) {
    this.dataType = type
  }

  override fun getDataType(): String? = dataType

  override fun setLatLng(value: String?) {
    latLng = value
  }

  override fun getPage(): Int? = page

  override fun setPage(value: Int?) {
    page = value
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
