package org.migor.feedless.pipeline.transformer

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KClass

interface Transformer<C : Any, P : Any, O> {
  fun consumes(): KClass<C>
  fun produces(): KClass<P>
  suspend fun transform(input: Flow<C>, parameters: O): Flow<P>
}
