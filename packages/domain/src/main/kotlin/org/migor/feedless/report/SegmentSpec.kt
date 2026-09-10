package org.migor.feedless.report

import org.migor.feedless.repository.RepositoryId
import java.time.LocalDateTime

/**
 * Beschreibt, welche Dokumente ein Empfänger in einem Report bekommen soll.
 *
 * Heute wird sie aus der [Segmentation] abgeleitet, die der Nutzer beim
 * Anlegen mitgibt: Zeitfenster, Umkreis, Grösse. Sie ist bewusst eine eigene
 * Schicht und nicht die Segmentation selbst, weil hier später das
 * Empfehlungsprofil andockt - gelernte Vorlieben, Kategorien, Ausschlüsse.
 * Die Auswahl der Dokumente hängt dann an dieser Beschreibung, nicht an der
 * Persistenzform, aus der sie stammt.
 */
data class SegmentSpec(
  val repositoryId: RepositoryId,
  val from: LocalDateTime,
  val until: LocalDateTime,
  val maxSize: Int,
  val near: NearFilter? = null,
  val tags: List<String> = emptyList(),
)

data class NearFilter(
  val lat: Double,
  val lng: Double,
  val distanceKm: Double,
)

/**
 * Das Fenster reicht von jetzt bis ein Intervall in die Zukunft: ein Report
 * kündigt an, was ansteht, statt zu berichten, was war.
 */
fun Segmentation.toSpec(now: LocalDateTime): SegmentSpec = SegmentSpec(
  repositoryId = repositoryId,
  from = now,
  until = now.plus(1, timeInterval),
  maxSize = size,
  near = contentSegmentLatLon?.let { point ->
    contentSegmentLatLonDistance?.let { distance ->
      NearFilter(lat = point.latitude, lng = point.longitude, distanceKm = distance)
    }
  },
)
