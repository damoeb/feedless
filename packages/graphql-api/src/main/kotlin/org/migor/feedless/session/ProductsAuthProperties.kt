package org.migor.feedless.session

import org.migor.feedless.AppProfiles
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.validation.annotation.Validated

data class ProductAuthProperties(
  val oauth: Boolean,
  val mailToken: Boolean = false,
)

/** Which login methods each product vertical offers. */
@ConfigurationProperties("app.auth.products")
@Validated
@Profile(AppProfiles.properties)
data class ProductsAuthProperties(
  val untold: ProductAuthProperties,
  val rssProxy: ProductAuthProperties,
  val feedless: ProductAuthProperties,
  val upcoming: ProductAuthProperties,
  val visualDiff: ProductAuthProperties,
)
