package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.module.Module
import java.io.Serializable
import java.util.*

interface FeedlessModule: Module, Serializable {

  fun getStartingAt(): Date?

  fun setStartingAt(date: Date?)

  fun setLatLng(value: String?)
  fun getLatLng(): String?
  fun getPage(): Int?
  fun setPage(value: Int?)
}
