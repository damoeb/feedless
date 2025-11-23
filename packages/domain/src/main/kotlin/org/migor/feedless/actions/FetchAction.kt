package org.migor.feedless.actions

import org.migor.feedless.source.PuppeteerWaitUntil
import org.migor.feedless.source.SourceId
import java.time.LocalDateTime
import java.util.*

data class FetchAction(
    override val id: ScrapeActionId = ScrapeActionId(UUID.randomUUID()),
    override val pos: Int? = null,
    override val sourceId: SourceId,
    val timeout: Int? = null,
    val url: String,
    val language: String? = null,
    val forcePrerender: Boolean = false,
    val isVariable: Boolean = false,
    val viewportWidth: Int? = null,
    val viewportHeight: Int? = null,
    val isMobile: Boolean = false,
    val isLandscape: Boolean = false,
    val waitUntil: PuppeteerWaitUntil? = null,
    val additionalWaitSec: Int? = null,
    override val createdAt: LocalDateTime = LocalDateTime.now()
) : ScrapeAction(id, pos, sourceId, createdAt)

