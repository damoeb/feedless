package org.migor.feedless.session

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.context.annotation.Configuration

data class ProductAuthProperties(
  val oauth: Boolean = false,
  val mailToken: Boolean = false
)

@Configuration
@ConfigurationProperties(prefix = "app.auth.products")
class ProductsAuthProperties {

  @NestedConfigurationProperty
  lateinit var untold: ProductAuthProperties

  @NestedConfigurationProperty
  lateinit var feedless: ProductAuthProperties

  @NestedConfigurationProperty
  lateinit var upcoming: ProductAuthProperties

  @NestedConfigurationProperty
  lateinit var visualDiff: ProductAuthProperties

}
