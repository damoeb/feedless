package org.migor.feedless.source

import org.migor.feedless.document.GeoPointWhereInput
import org.migor.feedless.document.StringFilter

data class SourcesFilter(
  val id: StringFilter? = null,
  val latLng: GeoPointWhereInput? = null,
  val like: String? = null,
  val disabled: Boolean? = null,
)
