package org.migor.rich.rss.config

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.api.ApiUrls
import org.migor.rich.rss.auth.AuthService
import org.migor.rich.rss.auth.CookieProvider
import org.migor.rich.rss.auth.JwtRequestFilter
import org.migor.rich.rss.auth.TokenProvider
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@PropertySource("classpath:application.properties")
class SecurityConfig {

  private val log = LoggerFactory.getLogger(SecurityConfig::class.simpleName)

  @Autowired
  lateinit var authService: AuthService

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var jwtRequestFilter: JwtRequestFilter

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var environment: Environment

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

//  @Bean
//  fun richClientRegistration(
//    @Value("\${spring.security.oauth2.client.provider.rich.authorization-uri}") authorization_uri: String,
//    @Value("\${spring.security.oauth2.client.provider.rich.token-uri}") tokenUri: String,
//    @Value("\${spring.security.oauth2.client.registration.rich.redirect-uri}") redirectUri: String,
//    @Value("\${spring.security.oauth2.client.registration.rich.authorization-grant-type}") authorizationGrantType: String,
//    @Value("\${spring.security.oauth2.client.registration.rich.client-authentication-method}") clientAuthenticationMethod: String,
//    @Value("\${spring.security.oauth2.client.registration.rich.clientId}") clientId: String,
//  ): ClientRegistration {
//    return ClientRegistration.withRegistrationId("rich")
//      .clientId(clientId)
//      .scope("openid", "profile", "email")
//      .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE) // todo authorizationGrantType
//      .authorizationUri(authorization_uri)
//      .tokenUri(tokenUri)
//      .userInfoAuthenticationMethod(AuthenticationMethod.QUERY)
//      .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
//      .redirectUri(redirectUri)
//      .build()
//  }

  @Throws(Exception::class)
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    return http
//      .authenticationManager(AuthenticationManagerBuilder())
      .sessionManagement()
//      .sessionFixation()
      .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
      .and()
      .cors().configurationSource(corsConfigurationSource()).and()
      .csrf().disable()
      .formLogin().disable()
      .httpBasic(Customizer.withDefaults())
      .addFilterAfter(jwtRequestFilter, OAuth2LoginAuthenticationFilter::class.java)
      .oauth2Login()
//      .redirectionEndpoint()
//        .baseUri("/login/oauth2/callback/*")
//      .and()
      .successHandler { request, response, authentication ->
        run {
          val attributes = (authentication.principal as DefaultOAuth2User).attributes
          val email = attributes["email"] as String
//          // other attributes: given_name, locale, first_name
          val name = attributes["name"] as String

          val user = userService.findByEmail(email)
            .orElseGet { userService.createUser(name, email) }
          log.info("jwt from user ${user.id}")
          val jwt = tokenProvider.createJwtForUser(user)
          response.addCookie(cookieProvider.createTokenCookie(jwt))
          response.addCookie(cookieProvider.createExpiredSessionCookie("JSESSION"))

          if (environment.acceptsProfiles(Profiles.of(AppProfiles.dev))) {
            response.sendRedirect("http://localhost:4200/?token=${jwt.tokenValue}")
          } else {
            response.sendRedirect(propertyService.appHost)
//            request.getRequestDispatcher("/").forward(request, response)
          }
        }
      }
      .failureHandler { _, _, exception -> log.error(exception.message) }
      .and()
      .authorizeHttpRequests()
      .requestMatchers(
        "/graphql",
        "/subscriptions",
        ApiUrls.login,
        ApiUrls.webToFeedFromRule,
        ApiUrls.webToFeedFromChange,
        "/login/oauth2/**",
        "/api/auth/magic-mail/**",
        "/bucket:*", "/bucket:*/*",
        "/feed:*", "/feed:*/*"
      ).permitAll()
      .requestMatchers("/actuator/**").hasRole("METRIC_ROLE")
      .and()
      .build()
  }

  fun corsConfigurationSource(): CorsConfigurationSource {
    val config = CorsConfiguration()
    config.allowedMethods = listOf("GET", "POST")
    config.addAllowedHeader("")
    config.allowCredentials = true
    config.addAllowedOriginPattern(CorsConfiguration.ALL)
    config.addAllowedHeader(CorsConfiguration.ALL)
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", config)
    return source
  }

  @Bean
  fun userDetailsService(@Value("\${app.actuatorPassword}") actuatorPassword: String): InMemoryUserDetailsManager {
    val user: UserDetails = User
      .withUsername("actuator")
      .password(passwordEncoder().encode(actuatorPassword))
      .roles("METRIC_ROLE")
      .build()
    return InMemoryUserDetailsManager(user)
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder(8)
  }
}
