package org.migor.feedless.util

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel

object JtsUtil {
  fun createPoint(lat: Double, lon: Double): Point {
    val gf = GeometryFactory(PrecisionModel(), 4326)
    return gf.createPoint(Coordinate(lat, lon))
  }
}
