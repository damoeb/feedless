package org.migor.feedless.http

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod

@Configuration
@Profile("${AppProfiles.session} & ${AppLayer.api}")
class HttpApiWebMvcConfiguration {

  @Bean
  fun httpApiWebMvcRegistrations(): WebMvcRegistrations =
    object : WebMvcRegistrations {
      override fun getRequestMappingHandlerAdapter(): RequestMappingHandlerAdapter =
        object : RequestMappingHandlerAdapter() {
          override fun createInvocableHandlerMethod(handlerMethod: HandlerMethod): ServletInvocableHandlerMethod =
            HttpApiServletInvocableHandlerMethod(handlerMethod)
        }
    }
}
