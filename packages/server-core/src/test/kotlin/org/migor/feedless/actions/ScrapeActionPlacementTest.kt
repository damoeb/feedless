package org.migor.feedless.actions

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.migor.feedless.data.jpa.source.toDomain
import org.migor.feedless.data.jpa.source.toEntity
import org.migor.feedless.source.ExtractEmit
import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.source.SourceId

class ScrapeActionPlacementTest {

  companion object {
    private val unplaced = SourceId()

    @JvmStatic
    fun actions(): List<ScrapeAction> = listOf(
      ClickPositionAction(sourceId = unplaced, x = 1, y = 2),
      ClickXpathAction(sourceId = unplaced, xpath = "//a"),
      DomAction(sourceId = unplaced, xpath = "//b", event = DomEventType.purge, data = "d"),
      ExecuteAction(sourceId = unplaced, pluginId = "org_feedless_filter", executorParams = PluginExecutionJson(paramsJsonString = "{}")),
      ExtractBoundingBoxAction(sourceId = unplaced, x = 1, fragmentName = "f", y = 2, w = 3, h = 4),
      ExtractXpathAction(sourceId = unplaced, fragmentName = "f", xpath = "//c", emit = arrayOf(ExtractEmit.html), uniqueBy = ExtractEmit.html),
      FetchAction(
        sourceId = unplaced,
        url = "https://example.org",
        timeout = 5,
        language = "de",
        forcePrerender = true,
        isVariable = true,
        viewportWidth = 800,
        viewportHeight = 600,
        isMobile = true,
        isLandscape = true,
        waitUntil = PuppeteerWaitUntil.entries.first(),
        additionalWaitSec = 2,
      ),
      HeaderAction(sourceId = unplaced, name = "Accept", value = "text/html"),
      WaitAction(sourceId = unplaced, xpath = "//d"),
    )
  }

  @ParameterizedTest
  @MethodSource("actions")
  fun `places an action exactly as the JPA round trip it replaced`(action: ScrapeAction) {
    val sourceId = SourceId()
    val roundTripped = action.toEntity().also {
      it.sourceId = sourceId.uuid
      it.pos = 3
    }.toDomain()

    val placed = action.placedAt(sourceId, 3)

    assertThat(placed).usingRecursiveComparison().isEqualTo(roundTripped)
  }
}
