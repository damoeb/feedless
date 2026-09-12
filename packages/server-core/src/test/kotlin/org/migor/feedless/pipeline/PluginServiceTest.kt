package org.migor.feedless.pipeline

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.migor.feedless.pipeline.plugins.ConditionalTagPlugin
import org.migor.feedless.pipeline.plugins.FeedPlugin
import org.migor.feedless.pipeline.plugins.FeedsPlugin
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.pipeline.plugins.PrivacyPlugin
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PluginServiceTest {

  @Test
  fun `given multiple plugins have the same id, postConstruct will fail`() {
    val plugin = mock(Plugin::class.java)
    `when`(plugin.id()).thenReturn("foo")

    val pluginService = PluginService(
      transformerPlugins = emptyList(),
      entityPlugins = emptyList(),
      plugins = listOf(plugin, plugin)
    )

    assertThatExceptionOfType(IllegalArgumentException::class.java).isThrownBy {
      pluginService.postConstruct()
    }
  }

  @ParameterizedTest
  @MethodSource("describedPlugins")
  fun `describeAll describes each plugin with its id, name, listing and transformer kind`(
    plugin: Plugin,
    expected: PluginDescriptor
  ) = runTest {
    // injected into the same lists Spring fills by type
    val pluginService = PluginService(
      entityPlugins = listOfNotNull(plugin as? MapEntityPlugin<*>),
      transformerPlugins = listOfNotNull(plugin as? FragmentTransformerPlugin),
      plugins = listOf(plugin)
    )

    assertThat(pluginService.describeAll()).containsOnly(expected)
  }

  companion object {
    @JvmStatic
    fun describedPlugins(): List<Arguments> = listOf(
      Arguments.of(
        ConditionalTagPlugin(),
        PluginDescriptor("org_feedless_conditional_tag", "Conditional Tags", listed = true, fragmentTransformer = false)
      ),
      Arguments.of(
        PrivacyPlugin(),
        PluginDescriptor("org_feedless_privacy", "Privacy & Robustness", listed = true, fragmentTransformer = false)
      ),
      Arguments.of(
        FeedPlugin(),
        PluginDescriptor("org_feedless_feed", "Feed", listed = true, fragmentTransformer = true)
      ),
      Arguments.of(
        FeedsPlugin(),
        PluginDescriptor("org_feedless_feeds", "Feeds", listed = false, fragmentTransformer = true)
      ),
      Arguments.of(
        FulltextPlugin(),
        PluginDescriptor("org_feedless_fulltext", "Fulltext & Readability", listed = true, fragmentTransformer = true)
      ),
    )
  }
}
