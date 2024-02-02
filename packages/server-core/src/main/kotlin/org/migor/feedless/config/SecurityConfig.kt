package org.migor.feedless.config

import io.micrometer.core.instrument.MeterRegistry
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.api.auth.CookieProvider
import org.migor.feedless.api.auth.JwtRequestFilter
import org.migor.feedless.api.auth.TokenProvider
import org.migor.feedless.data.jpa.enums.AuthSource
import org.migor.feedless.data.jpa.enums.ProductName
import org.migor.feedless.data.jpa.models.PlanName
import org.migor.feedless.data.jpa.models.UserEntity
import org.migor.feedless.service.PropertyService
import org.migor.feedless.service.UserService
import org.migor.feedless.util.CryptUtil.newCorrId
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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
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
//@Profile(AppProfiles.database)
class SecurityConfig {

  private val log = LoggerFactory.getLogger(SecurityConfig::class.simpleName)
  private val metricRole = "METRIC_CONSUMER"

  @Autowired(required = false)
  lateinit var userService: UserService

  @Autowired(required = false)
  lateinit var jwtRequestFilter: JwtRequestFilter

  @Autowired
  lateinit var propertyService: PropertyService

  @Autowired
  lateinit var tokenProvider: TokenProvider

  @Autowired
  lateinit var cookieProvider: CookieProvider

  @Autowired
  lateinit var meterRegistry: MeterRegistry

  @Autowired
  lateinit var environment: Environment

  @Throws(Exception::class)
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    return conditionalOauth(http)
//      .authenticationManager(AuthenticationManagerBuilder())
      .sessionManagement()
//      .sessionFixation()
      .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      .cors().configurationSource(corsConfigurationSource()).and()
      .csrf().disable()
      .formLogin().disable()
      .httpBasic(Customizer.withDefaults())
      .authorizeHttpRequests()
      .requestMatchers(*(whitelistedUrls())).permitAll()
      .requestMatchers("/actuator/**").hasAnyRole(metricRole)
      .and()
      .build()
  }

  private fun whitelistedUrls(): Array<String> {
    val urls = mutableListOf(
      "/graphql",
      "/subscriptions",
//        ApiUrls.login,
      ApiUrls.transformFeed,
      ApiUrls.webToFeed,
      ApiUrls.webToFeedVerbose,
      ApiUrls.webToFeedFromRule,
      ApiUrls.webToFeedFromChange,
      "/api/legacy/**",
      "/stream/feed/**",
      "/stream/bucket/**",
      "/feed/**",
      "/feed:**",
      "/bucket/**",
      "/bucket:**",
//      "/f/**",
      "/attachment/**",
      "/a/**",
    )
    if (propertyService.authentication == AppProfiles.authSSO) {
      urls.add("/login/oauth2/**")
    }
    if (propertyService.authentication == AppProfiles.authMail) {
      urls.add("/api/auth/magic-mail/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.serveStatic))) {
      urls.add("/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.testing))) {
      urls.add("/testing/**")
    }

    return urls.toTypedArray()
  }

  private fun conditionalOauth(http: HttpSecurity): HttpSecurity {
    return if (propertyService.authentication == AppProfiles.authSSO) {
      http
        .addFilterAfter(jwtRequestFilter, OAuth2LoginAuthenticationFilter::class.java)
        .oauth2Login()
        .successHandler { _, response, authentication ->
          run {
            val corrId = newCorrId()
            val authenticationToken = authentication as OAuth2AuthenticationToken
            val user = when(authenticationToken.authorizedClientRegistrationId) {
              "github" -> handleGithubAuthResponse(authenticationToken)
              "google" -> handleGoogleAuthResponse(authenticationToken)
              else -> throw IllegalAccessException("authorizedClientRegistrationId ${authenticationToken.authorizedClientRegistrationId} not supported")
            }
            log.info("jwt from user ${user.id}")
            val jwt = tokenProvider.createJwtForUser(user)
            response.addCookie(cookieProvider.createTokenCookie(corrId, jwt))
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
    } else {
      http
    }
  }

  private fun handleGithubAuthResponse(authentication: OAuth2AuthenticationToken): UserEntity {
    val attributes = (authentication.principal as DefaultOAuth2User).attributes
    val email = "${attributes["id"]}@github.com"
    return resolveUserByEmail(email) ?: userService.createUser(newCorrId(), email, authSource = AuthSource.oauth, plan = PlanName.minimal, productName = ProductName.feedless)
  }

  private fun handleGoogleAuthResponse(authentication: OAuth2AuthenticationToken): UserEntity {
    val attributes = (authentication.principal as DefaultOAuth2User).attributes
    val email = attributes["email"] as String
    return resolveUserByEmail(email) ?: userService.createUser(newCorrId(), email, authSource = AuthSource.oauth, plan = PlanName.minimal, productName = ProductName.feedless)
  }

  private fun resolveUserByEmail(email: String): UserEntity? {
    return userService.findByEmail(email)
      .also {
        it?.let {
          meterRegistry.counter(AppMetrics.userLogin).increment()
        }
      }
    }

  @Bean
  fun userDetailsService(@Value("\${app.actuatorPassword}") actuatorPassword: String): InMemoryUserDetailsManager {
    val user: UserDetails = User
      .withUsername("actuator")
      .password(passwordEncoder().encode(actuatorPassword))
      .roles(metricRole)
      .build()
    return InMemoryUserDetailsManager(user)
  }

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder(8)
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
}
