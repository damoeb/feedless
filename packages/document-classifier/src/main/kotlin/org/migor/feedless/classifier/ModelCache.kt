package org.migor.feedless.classifier

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Hält je Modellpfad genau eine geladene Instanz und serialisiert den Zugriff
 * darauf.
 *
 * Beides ist nötig. Das Laden ist teuer und lief bisher bei jedem einzelnen
 * Klassifizierungsaufruf. Und die geladene Instanz darf nicht nebenläufig
 * benutzt werden: JFastText hält einen nativen Zeiger, und fastText verwendet
 * beim Vorhersagen veränderliche Member-Vektoren im Modell. Threadsicherheit
 * ist dort weder dokumentiert noch von aussen belegbar, also wird sie hier
 * nicht vorausgesetzt.
 *
 * Der Preis ist ein Vorhersagevorgang zur Zeit je Modell. Bei einer Vorhersage
 * im Mikrosekundenbereich ist das gegenüber dem bisherigen Laden pro Aufruf
 * ein Gewinn, kein Verlust.
 */
class ModelCache<T>(private val load: (String) -> T) {

  private class Entry<T>(val model: T) {
    val mutex = Mutex()
  }

  private val entries = ConcurrentHashMap<String, Entry<T>>()

  /**
   * Führt [block] mit dem Modell zu [path] aus. Das Modell wird beim ersten
   * Zugriff geladen, danach wiederverwendet.
   */
  suspend fun <R> withModel(path: String, block: (T) -> R): R {
    val entry = entries.computeIfAbsent(path) { Entry(load(it)) }
    return entry.mutex.withLock { block(entry.model) }
  }

  /** Anzahl geladener Modelle. Für Tests und Diagnose. */
  fun size(): Int = entries.size
}
