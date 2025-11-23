package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.ConditionalTagPlugin
import org.migor.feedless.pipeline.plugins.DiffRecordsPlugin
import org.migor.feedless.pipeline.plugins.EventsReportPlugin
import org.migor.feedless.pipeline.plugins.FeedPlugin
import org.migor.feedless.pipeline.plugins.FeedsPlugin
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.pipeline.plugins.PrivacyPlugin
import org.migor.feedless.generated.types.Plugin as PluginDto
import org.migor.feedless.generated.types.PluginType as PluginTypeDto

class PluginResolverTest {

    @Test
    fun testCompositeFilterPluginToDto() {
        val incoming = CompositeFilterPlugin()
        val expected = PluginDto(
            id = "org_feedless_filter",
            name = "Filter",
            type = PluginTypeDto.entity,
            listed = true,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testConditionalTagPluginToDto() {
        val incoming = ConditionalTagPlugin()
        val expected = PluginDto(
            id = "org_feedless_conditional_tag",
            name = "Conditional Tags",
            type = PluginTypeDto.entity,
            listed = true,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testDiffRecordsPluginToDto() {
        val incoming = DiffRecordsPlugin()
        val expected = PluginDto(
            id = "org_feedless_diff_records",
            name = "",
            type = PluginTypeDto.entity,
            listed = false,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testEventsReportPluginToDto() {
        val incoming = EventsReportPlugin()
        val expected = PluginDto(
            id = "org_feedless_event_report",
            name = "",
            type = PluginTypeDto.entity,
            listed = false,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testFeedPluginToDto() {
        val incoming = FeedPlugin()
        val expected = PluginDto(
            id = "org_feedless_feed",
            name = "Feed",
            type = PluginTypeDto.fragment,
            listed = true,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testFeedsPluginToDto() {
        val incoming = FeedsPlugin()
        val expected = PluginDto(
            id = "org_feedless_feeds",
            name = "Feeds",
            type = PluginTypeDto.fragment,
            listed = false,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }

    @Test
    fun testFulltextPluginToDto() {
        val incoming = FulltextPlugin()
        val expected = PluginDto(
            id = "org_feedless_fulltext",
            name = "Fulltext & Readability",
            type = PluginTypeDto.fragment,
            listed = true,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }


    @Test
    fun testPrivacyPluginToDto() {
        val incoming = PrivacyPlugin()
        val expected = PluginDto(
            id = "org_feedless_privacy",
            name = "Privacy & Robustness",
            type = PluginTypeDto.entity,
            listed = true,
        )
        assertThat(incoming.toDto()).isEqualTo(expected)
    }


}
