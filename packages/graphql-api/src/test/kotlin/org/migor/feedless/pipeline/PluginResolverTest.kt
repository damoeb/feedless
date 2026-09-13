package org.migor.feedless.pipeline

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.migor.feedless.generated.types.Plugin as PluginDto
import org.migor.feedless.generated.types.PluginType as PluginTypeDto

class PluginResolverTest {

  @Test
  fun `a listed entity plugin maps to a listed entity dto`() {
    val incoming = PluginDescriptor(id = "org_feedless_filter", name = "Filter", listed = true, fragmentTransformer = false)
    val expected = PluginDto(
      id = "org_feedless_filter",
      name = "Filter",
      type = PluginTypeDto.entity,
      listed = true,
    )
    assertThat(incoming.toDto()).isEqualTo(expected)
  }

  @Test
  fun `an unlisted entity plugin maps to an unlisted entity dto`() {
    val incoming = PluginDescriptor(id = "org_feedless_diff_records", name = "", listed = false, fragmentTransformer = false)
    val expected = PluginDto(
      id = "org_feedless_diff_records",
      name = "",
      type = PluginTypeDto.entity,
      listed = false,
    )
    assertThat(incoming.toDto()).isEqualTo(expected)
  }

  @Test
  fun `a listed fragment transformer maps to a listed fragment dto`() {
    val incoming = PluginDescriptor(id = "org_feedless_feed", name = "Feed", listed = true, fragmentTransformer = true)
    val expected = PluginDto(
      id = "org_feedless_feed",
      name = "Feed",
      type = PluginTypeDto.fragment,
      listed = true,
    )
    assertThat(incoming.toDto()).isEqualTo(expected)
  }

  @Test
  fun `an unlisted fragment transformer maps to an unlisted fragment dto`() {
    val incoming = PluginDescriptor(id = "org_feedless_feeds", name = "Feeds", listed = false, fragmentTransformer = true)
    val expected = PluginDto(
      id = "org_feedless_feeds",
      name = "Feeds",
      type = PluginTypeDto.fragment,
      listed = false,
    )
    assertThat(incoming.toDto()).isEqualTo(expected)
  }
}
