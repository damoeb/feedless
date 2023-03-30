package org.migor.rich.rss.config

import com.netflix.graphql.dgs.DgsQueryExecutor
import com.netflix.graphql.dgs.subscriptions.websockets.DgsWebSocketConfigurationProperties
import com.netflix.graphql.dgs.subscriptions.websockets.DgsWebSocketHandler
import org.migor.rich.rss.auth.AuthenticationHttpSessionHandshakeInterceptor
import org.migor.rich.rss.auth.AuthService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Configuration
@EnableConfigurationProperties(DgsWebSocketConfigurationProperties::class)
class CustomDgsWebSocketAutoConfig {

  @Bean
  fun webSocketHandler(@Suppress("SpringJavaInjectionPointsAutowiringInspection") dgsQueryExecutor: DgsQueryExecutor, configProps: DgsWebSocketConfigurationProperties): WebSocketHandler {
    return DgsWebSocketHandler(dgsQueryExecutor, configProps.connectionInitTimeout, configProps.subscriptionErrorLogLevel)
  }

  @Configuration
  @EnableWebSocketSecurity
  @EnableWebSocket
  internal class WebSocketConfig(
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val webSocketHandler: WebSocketHandler,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val configProps: DgsWebSocketConfigurationProperties,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") private val authService: AuthService
  ) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
      registry.addHandler(webSocketHandler, configProps.path)
        .addInterceptors(AuthenticationHttpSessionHandshakeInterceptor(authService))
        .setAllowedOrigins("*")
    }
  }
}
