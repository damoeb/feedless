package org.migor.feedless.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner

/** Binds properties classes against the real application.yaml, so legacy keys and env vars keep working. */
class LegacyConfigBindingTest {

  // env vars resolve like any other property source, so a property value stands in for one
  private inline fun <reified T : Any> bind(vararg legacyValues: String): T {
    val prefix = T::class.java.getAnnotation(ConfigurationProperties::class.java).value
    var bound: T? = null
    ApplicationContextRunner()
      .withInitializer(ConfigDataApplicationContextInitializer())
      .withPropertyValues(*legacyValues)
      .run { context -> bound = Binder.get(context.environment).bindOrCreate(prefix, T::class.java) }
    return bound!!
  }
}
