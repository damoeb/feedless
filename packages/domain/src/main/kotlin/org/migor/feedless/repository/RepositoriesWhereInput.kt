package org.migor.feedless.repository

import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical

data class RepositoriesWhereInput(
    val product: VerticalFilter? = null,
    val visibility: VisibilityFilter? = null,
    val text: FulltextQueryFilter? = null,
    val tags: StringArrayFilter? = null,
)

data class VerticalFilter(
    val eq: Vertical? = null,
    val `in`: List<Vertical>? = null,
)

data class VisibilityFilter(
    val `in`: List<EntityVisibility>? = null,
)

data class FulltextQueryFilter(
    val query: String,
)

data class StringArrayFilter(
    val every: List<String>? = null,
    val some: List<String>? = null,
)

