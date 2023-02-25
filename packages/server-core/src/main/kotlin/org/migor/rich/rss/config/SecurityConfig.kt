package org.migor.rich.rss.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthenticationMethod
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.web.SecurityFilterChain


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@PropertySource("classpath:application.properties")
class SecurityConfig {

  private val log = LoggerFactory.getLogger(SecurityConfig::class.simpleName)

//  @Throws(Exception::class)
//  protected fun configure(auth: AuthenticationManagerBuilder) {
////    val encoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
////    auth
////      .inMemoryAuthentication()
////      .withUser("user")
////      .password(encoder.encode("password"))
////      .roles("USER")
////      .and()
////      .withUser("admin")
////      .password(encoder.encode("admin"))
////      .roles("USER", "ADMIN")
//  }

  @Bean
  fun richClientRegistration(
    @Value("\${spring.security.oauth2.client.provider.rich.authorization-uri}") authorization_uri: String,
    @Value("\${spring.security.oauth2.client.provider.rich.token-uri}") tokenUri: String,
    @Value("\${spring.security.oauth2.client.registration.rich.redirect-uri}") redirectUri: String,
    @Value("\${spring.security.oauth2.client.registration.rich.authorization-grant-type}") authorizationGrantType: String,
    @Value("\${spring.security.oauth2.client.registration.rich.client-authentication-method}") clientAuthenticationMethod: String,
    @Value("\${spring.security.oauth2.client.registration.rich.clientId}") clientId: String,
  ): ClientRegistration {
    return ClientRegistration.withRegistrationId("rich")
      .clientId(clientId)
      .scope("openid", "profile", "email")
      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // todo authorizationGrantType
      .authorizationUri(authorization_uri)
      .tokenUri(tokenUri)
      .userInfoAuthenticationMethod(AuthenticationMethod.QUERY)
      .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
      .redirectUri(redirectUri)
      .build()
  }

//  private fun googleClientRegistration(): ClientRegistration {
//    return ClientRegistration.withRegistrationId("google")
//      .clientId("google-client-id")
//      .clientSecret("google-client-secret")
//      .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
//      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
//      .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
//      .scope("openid", "profile", "email", "address", "phone")
//      .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
//      .tokenUri("https://www.googleapis.com/oauth2/v4/token")
//      .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
//      .userNameAttributeName(IdTokenClaimNames.SUB)
//      .jwkSetUri("https://www.googleapis.com/oauth2/v3/certs")
//      .clientName("Google")
//      .build();
//  }

//  @Bean
//  fun webSecurityCustomizer(): WebSecurityCustomizer {
//    return WebSecurityCustomizer { web: WebSecurity -> web.ignoring().requestMatchers("/ignore1", "/ignore2") }
//  }

  @Throws(Exception::class)
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .sessionManagement()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .oauth2Login()
      .tokenEndpoint()
//      .accessTokenResponseClient(DefaultAuthorizationCodeTokenResponseClient())
      .and()
      .and()
      .csrf().disable()
      .anonymous()
//      .authorizeHttpRequests()
////      .antMatchers("/actuator/**").hasRole("ENDPOINT_ADMIN")
//      .requestMatchers("/api/intern/**").authenticated()
//      .requestMatchers("/api/oauth2/**").anonymous()
      .and()
      .build()
  }
}
