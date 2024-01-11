package org.migor.feedless.data.jpa.models

enum class FeatureState {
  off,
  experimental,
  beta,
  stable
}

fun FeatureState.toDto(): org.migor.feedless.generated.types.FeatureState = when (this) {
  FeatureState.off -> org.migor.feedless.generated.types.FeatureState.off
  FeatureState.beta -> org.migor.feedless.generated.types.FeatureState.beta
  FeatureState.experimental -> org.migor.feedless.generated.types.FeatureState.experimental
  FeatureState.stable -> org.migor.feedless.generated.types.FeatureState.stable
}
