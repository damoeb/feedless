package org.migor.feedless.config

import io.micrometer.core.instrument.MeterRegistry
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.BadRequestException
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.common.PropertyService
import org.migor.feedless.data.jpa.enums.ProductCategory
import org.migor.feedless.plan.PlanName
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtRequestFilter
import org.migor.feedless.session.TokenProvider
import org.migor.feedless.user.UserEntity
import org.migor.feedless.user.UserService
import org.migor.feedless.util.CryptUtil.newCorrId
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@PropertySource("classpath:application.properties")
@Profile(AppProfiles.api)
class SecurityConfig {

  private val log = LoggerFactory.getLogger(SecurityConfig::class.simpleName)
  private val metricRole = "METRIC_CONSUMER"

  @Value("\${app.cors.allowedOrigins:}")
  lateinit var allowedOrigins: String

  @Autowired
  private lateinit var userService: UserService

  @Autowired(required = false)
  lateinit var jwtRequestFilter: JwtRequestFilter

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
      .csrf().disable()
      .formLogin().disable()
      .httpBasic(Customizer.withDefaults())
      .authorizeHttpRequests()
      .requestMatchers(*(whitelistedUrls())).permitAll()
      .requestMatchers("/actuator/**").hasAnyRole(metricRole)
      .requestMatchers("/actuator/prometheus").hasAnyRole(metricRole)
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
      ApiUrls.mailForwardingAllow + "/**",
      "/api/legacy/**",
      "/stream/feed/**",
      "/payment/**",
//      "/stream/feed/*/*",
      "/stream/bucket/**",
//      "/stream/bucket/*/*",
      "/feed/**",
      "/f/**",
//      "/feed/*/*",
      "/feed:**",
//      "/feed:*/*",
      "/article/**",
      "/a/**",
      "/bucket/*",
      "/bucket/*/*",
      "/bucket:*",
      "/bucket:*/*",
      "/attachment/**",
    )
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.authSSO))) {
      urls.add("/login/oauth2/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.authMail))) {
      urls.add("/api/auth/magic-mail/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.serveStatic))) {
      urls.add("/**")
    }
    if (environment.acceptsProfiles(Profiles.of(AppProfiles.dev))) {
      urls.add("/testing/**")
    }

    return urls.toTypedArray()
  }

  private fun conditionalOauth(http: HttpSecurity): HttpSecurity {
    return if (environment.acceptsProfiles(Profiles.of(AppProfiles.authSSO))) {
      http
        .addFilterAfter(jwtRequestFilter, OAuth2LoginAuthenticationFilter::class.java)
        .oauth2Login()
        .successHandler { _, response, authentication ->
          run {
            val corrId = newCorrId()
            val authenticationToken = authentication as OAuth2AuthenticationToken
            val user = when (authenticationToken.authorizedClientRegistrationId) {
              "github" -> handleGithubAuthResponse(authenticationToken)
//              "google" -> handleGoogleAuthResponse(authenticationToken)
              else -> throw BadRequestException("authorizedClientRegistrationId ${authenticationToken.authorizedClientRegistrationId} not supported")
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
    val email = attributes["email"] as String?
    val githubId = (attributes["id"] as Int).toString()
    return resolveUserByGithubId(githubId) ?: userService.createUser(
      newCorrId(),
      email = email,
      githubId = githubId,
      planName = PlanName.free,
      productCategory = ProductCategory.feedless
    )
  }

  private fun handleGoogleAuthResponse(authentication: OAuth2AuthenticationToken): UserEntity {
    val attributes = (authentication.principal as DefaultOAuth2User).attributes
    val email = attributes["email"] as String
    return resolveUserByEmail(email) ?: userService.createUser(
      newCorrId(),
      email,
      planName = PlanName.free,
      productCategory = ProductCategory.feedless
    )
  }

  private fun resolveUserByEmail(email: String): UserEntity? {
    return userService.findByEmail(email)
      .also {
        it?.let {
          meterRegistry.counter(AppMetrics.userLogin).increment()
        }
      }
  }

  private fun resolveUserByGithubId(githubId: String): UserEntity? {
    return userService.findByGithubId(githubId)
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
