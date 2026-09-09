package org.migor.feedless.classifier

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

/**
 * Der Klassifikator lud das Modell bisher bei jedem einzelnen Aufruf von der
 * Platte. Diese Tests halten die beiden Eigenschaften fest, die das ersetzt:
 * einmal laden je Pfad, und nie nebenläufig auf derselben Instanz arbeiten.
 */
class ModelCacheTest {

  @Test
  fun `loads a model once and reuses it`() = runTest {
    val loads = AtomicInteger()
    val cache = ModelCache { path -> "$path:${loads.incrementAndGet()}" }

    val first = cache.withModel("model-a") { it }
    val second = cache.withModel("model-a") { it }

    assertThat(loads.get()).isEqualTo(1)
    assertThat(first).isEqualTo(second)
    assertThat(cache.size()).isEqualTo(1)
  }

  @Test
  fun `keeps one instance per model path`() = runTest {
    val loads = AtomicInteger()
    val cache = ModelCache { path -> "$path:${loads.incrementAndGet()}" }

    cache.withModel("model-a") { it }
    cache.withModel("model-b") { it }
    cache.withModel("model-a") { it }

    assertThat(loads.get()).isEqualTo(2)
    assertThat(cache.size()).isEqualTo(2)
  }

  @Test
  fun `loads a model once even under concurrent first access`() = runTest {
    val loads = AtomicInteger()
    val cache = ModelCache { path -> "$path:${loads.incrementAndGet()}" }

    withContext(Dispatchers.Default) {
      (1..32).map { async { cache.withModel("model-a") { it } } }.awaitAll()
    }

    assertThat(loads.get()).isEqualTo(1)
  }

  /**
   * Der eigentliche Zweck: JFastText hält einen nativen Zeiger, und fastText
   * arbeitet beim Vorhersagen auf veränderlichen Member-Vektoren. Zwei
   * gleichzeitige Vorhersagen auf derselben Instanz wären ein Datenrennen.
   */
  @Test
  fun `never runs two blocks on the same model at once`() = runTest {
    val cache = ModelCache { it }
    val inFlight = AtomicInteger()
    val maxInFlight = AtomicInteger()

    withContext(Dispatchers.Default) {
      (1..16).map {
        async {
          cache.withModel("model-a") {
            val now = inFlight.incrementAndGet()
            maxInFlight.updateAndGet { max -> maxOf(max, now) }
            Thread.sleep(2)
            inFlight.decrementAndGet()
          }
        }
      }.awaitAll()
    }

    assertThat(maxInFlight.get()).isEqualTo(1)
  }

  @Test
  fun `lets different models run at the same time`() = runTest {
    val cache = ModelCache { it }
    val started = AtomicInteger()

    withContext(Dispatchers.Default) {
      listOf("model-a", "model-b").map { path ->
        async {
          cache.withModel(path) {
            started.incrementAndGet()
            // Wartet, bis der andere Pfad ebenfalls gestartet ist. Bei einer
            // globalen Sperre statt einer je Modell liefe das in den Timeout.
            while (started.get() < 2) {
              Thread.sleep(1)
            }
          }
        }
      }.awaitAll()
    }

    assertThat(started.get()).isEqualTo(2)
  }

  @Test
  fun `propagates a failure to load and does not cache it`() = runTest {
    val attempts = AtomicInteger()
    val cache = ModelCache<String> {
      attempts.incrementAndGet()
      throw IllegalArgumentException("Model file doesn't exist!")
    }

    repeat(2) {
      runCatching { cache.withModel("missing") { it } }
    }

    assertThat(attempts.get()).isEqualTo(2)
    assertThat(cache.size()).isEqualTo(0)
    delay(0)
  }
}
