package org.migor.feedless.feed

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel
import org.migor.feedless.feed.parser.json.JsonPoint
import org.migor.feedless.geo.LatLonPoint

fun JsonPoint.toPoint(): Point {
  return createPoint(x, y)
}

fun LatLonPoint.toPoint(): Point {
  return createPoint(latitude, longitude)
}

// Copy of jpa-data's JtsUtil.createPoint, which domain cannot depend on.
private fun createPoint(lat: Double, lon: Double): Point {
  val gf = GeometryFactory(PrecisionModel(), 4326)
  return gf.createPoint(Coordinate(lat, lon))
}
