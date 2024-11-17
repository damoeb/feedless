package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class PluginServiceTest {

  @Test
  fun `given multiple plugins have the same id, postConstruct will fail`() {
    val plugin = mock(FeedlessPlugin::class.java)
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
}
