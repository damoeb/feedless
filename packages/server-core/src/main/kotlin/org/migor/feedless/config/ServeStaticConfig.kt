package org.migor.feedless.config

import org.migor.feedless.AppProfiles
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
@Profile(AppProfiles.serveStatic)
class ServeStaticConfig : WebMvcConfigurer {

  private val log = LoggerFactory.getLogger(ServeStaticConfig::class.simpleName)

  init {
    log.info("Serving static from public/")
  }


  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry
      .addResourceHandler("/**")
      .addResourceLocations("file:public/", "classpath:/public/")
  }
}
