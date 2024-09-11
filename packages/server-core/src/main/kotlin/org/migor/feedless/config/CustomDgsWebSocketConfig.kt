package org.migor.feedless.config

import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.subscriptions.websockets.DgsWebSocketConfigurationProperties
import com.netflix.graphql.dgs.subscriptions.websockets.DgsWebSocketHandler
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.session.AuthenticationHttpSessionHandshakeInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry


@Configuration
@Profile(AppLayer.api)
@EnableConfigurationProperties(DgsWebSocketConfigurationProperties::class)
class CustomDgsWebSocketConfig {

//  @Bean
//  fun dgsReactiveCustomContextBuilder(): DgsReactiveCustomContextBuilderWithRequest<CustomContext> {
//    return DgsReactiveCustomContextBuilderWithRequest<CustomContext> { map, httpHeaders, serverRequest ->
//      ReactiveSecurityContextHolder.getContext()
//        .map<Any> { securityContext: SecurityContext? ->
//          CustomContext(
//            serverRequest,
//            securityContext
//          )
//        }
//        .defaultIfEmpty(CustomContext(serverRequest, null))
//    }
//  }

  @Bean
  fun webSocketHandler(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") dgsQueryExecutor: DgsQueryExecutor,
    configProps: DgsWebSocketConfigurationProperties
  ): WebSocketHandler {
    return DgsWebSocketHandler(
      dgsQueryExecutor,
      configProps.connectionInitTimeout,
      configProps.subscriptionErrorLogLevel
    )
  }

  @Configuration
  @EnableWebSocketSecurity
  @EnableWebSocket
  @Profile(AppLayer.api)
  internal class WebSocketConfig(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val webSocketHandler: WebSocketHandler,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val configProps: DgsWebSocketConfigurationProperties,
  ) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
      registry.addHandler(webSocketHandler, configProps.path)
        .addInterceptors(AuthenticationHttpSessionHandshakeInterceptor())
        .setAllowedOrigins("*")
    }
  }
}
