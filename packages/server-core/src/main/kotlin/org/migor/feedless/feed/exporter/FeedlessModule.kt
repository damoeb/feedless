package org.migor.feedless.feed.exporter

import com.rometools.rome.feed.module.Module
import org.migor.feedless.generated.types.GeoPoint
import java.io.Serializable
import java.util.*

interface FeedlessModule: Module, Serializable {

  fun getStartingAt(): Date?

  fun setStartingAt(date: Date?)

  fun setLatLng(value: GeoPoint?)
  fun getLatLng(): GeoPoint?
}
