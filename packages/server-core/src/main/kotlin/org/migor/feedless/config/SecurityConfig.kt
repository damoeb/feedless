package org.migor.feedless.config

import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.common.PropertyService
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtRequestFilter
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
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
import org.springframework.web.context.request.RequestContextListener
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@PropertySource("classpath:application.yaml")
@Profile(AppLayer.security)
class SecurityConfig {

  private val log = LoggerFactory.getLogger(SecurityConfig::class.simpleName)
  private val metricRole = "METRIC_CONSUMER"

  @Value("\${app.cors.allowedOrigins:}")
  lateinit var allowedOrigins: String

  @Autowired(required = false)
  private var userService: UserService? = null

  @Autowired
  private lateinit var jwtRequestFilter: JwtRequestFilter

  @Autowired
  private lateinit var propertyService: PropertyService

  @Autowired
  private lateinit var tokenProvider: TokenProvider

  @Autowired
  private lateinit var cookieProvider: CookieProvider

  @Autowired
  private lateinit var meterRegistry: MeterRegistry

  @Autowired
  private lateinit var environment: Environment

  @Throws(Exception::class)
  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    return conditionalOauth(http)
      .headers {
        it.httpStrictTransportSecurity {
          it.includeSubDomains(true)
        }

      }
      .sessionManagement {
        it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      }
      .cors {
        it.configurationSource(corsConfigurationSource())
      }
      .csrf { it.disable() }
      .formLogin { it.disable() }
      .httpBasic(Customizer.withDefaults())
      .authorizeHttpRequests {
        it.requestMatchers(*(whitelistedUrls())).permitAll()
        it.requestMatchers("/actuator/**").hasAnyRole(metricRole)
        it.requestMatchers("/actuator/prometheus").hasAnyRole(metricRole)
      }
      .build()
  }

  private fun whitelistedUrls(): Array<String> {
    val urls = mutableListOf(
      "/graphql",
      "/actuator/health",
      "/subscriptions",
      ApiUrls.transformFeed,
      ApiUrls.webToFeed,
      ApiUrls.webToFeedVerbose,
      ApiUrls.mailForwardingAllow + "/**",
      "/stream/feed/**",
      "/api/feed**",
      "/feed/**",
      "/f/**",
      "/feed:**",
      "/payment/**",
      "/stream/bucket/**",
      "/bucket/**",
      "/bucket:**",
      "/article/**",
      "/a/**",
      "/attachment/**",
    )
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.oauth))) {
      urls.add("/login/oauth2/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.mail))) {
      urls.add("/api/auth/magic-mail/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.DEV_ONLY))) {
      urls.add("/testing/**")
    }

    return urls.toTypedArray()
  }

  @Bean
  fun requestContextListener(): RequestContextListener {
    return RequestContextListener()
  }

  private fun conditionalOauth(http: HttpSecurity): HttpSecurity {
    return if (environment.acceptsProfiles(Profiles.of(AppProfiles.oauth))) {
      http
        .addFilterAfter(jwtRequestFilter, OAuth2LoginAuthenticationFilter::class.java)
        .oauth2Login {
          it.successHandler { _, response, authentication ->
            runBlocking {
              coroutineScope {
                val authenticationToken = authentication as OAuth2AuthenticationToken
                val user = when (authenticationToken.authorizedClientRegistrationId) {
                  "github" -> handleGithubAuthResponse(authenticationToken)
                  else -> throw BadRequestException("authorizedClientRegistrationId ${authenticationToken.authorizedClientRegistrationId} not supported")
                }
                log.info("jwt from user ${user.id}")
                val jwt = tokenProvider.createJwtForUser(user)
                response.addCookie(cookieProvider.createTokenCookie(jwt))
                response.addCookie(cookieProvider.createExpiredSessionCookie("JSESSION"))
//
                if (environment.acceptsProfiles(Profiles.of(AppProfiles.DEV_ONLY))) {
                  response.sendRedirect("http://localhost:4200/?token=${jwt.tokenValue}")
                } else {
                  response.sendRedirect(propertyService.appHost)
//            request.getRequestDispatcher("/").forward(request, response)
                }
              }
            }
          }
          it.failureHandler { _, response, exception ->
            log.error("conditionalOauth failed: ${exception.message}", exception)
            response.sendRedirect(propertyService.appHost + "/login?error=${exception.message}")
          }
        }

    } else {
      http
    }
  }

  private suspend fun handleGithubAuthResponse(authentication: OAuth2AuthenticationToken): UserEntity {
    val attributes = (authentication.principal as DefaultOAuth2User).attributes
    val email = attributes["email"] as String?
    val githubId = (attributes["id"] as Int).toString()
    return resolveUserByGithubId(githubId)?.also { userService!!.updateLegacyUser(it, githubId) }
      ?: userService!!.createUser(
        email = email,
        githubId = githubId,
      )
  }

  private suspend fun resolveUserByGithubId(githubId: String): UserEntity? {
    return userService!!.findByGithubId(githubId)
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
    config.allowCredentials = true
    config.allowedHeaders = listOf(CorsConfiguration.ALL)
    config.allowedOrigins = StringUtils.trimToNull(allowedOrigins)?.split(",")?.map { it.trim() }
    log.info("cors allowedOrigins = [${config.allowedOrigins?.joinToString(",")}]")
    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", config)
    return source
  }
}
