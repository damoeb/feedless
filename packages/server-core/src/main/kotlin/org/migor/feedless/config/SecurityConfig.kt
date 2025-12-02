package org.migor.feedless.config

import io.micrometer.core.instrument.MeterRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.StringUtils
import org.migor.feedless.AppLayer
import org.migor.feedless.AppMetrics
import org.migor.feedless.AppProfiles
import org.migor.feedless.api.ApiUrls
import org.migor.feedless.capability.GroupCapability
import org.migor.feedless.capability.UserCapability
import org.migor.feedless.connector.github.GithubCapability
import org.migor.feedless.session.CookieProvider
import org.migor.feedless.session.JwtRequestFilter
import org.migor.feedless.session.JwtTokenIssuer
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
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
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.context.request.RequestContextListener
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.net.URI
import org.springframework.security.core.userdetails.User as BasicAuthUser
import org.springframework.security.core.userdetails.UserDetails as BasicAuthUserDetails


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
  private var userUseCase: UserUseCase? = null

  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var jwtRequestFilter: JwtRequestFilter

  @Autowired
  private lateinit var authorizedClientService: OAuth2AuthorizedClientService

  @Autowired
  private lateinit var jwtTokenIssuer: JwtTokenIssuer

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
          it.successHandler { request, response, authentication ->
            handleSuccess(request, response, authentication)
          }
          it.failureHandler { request, response, exception ->
            log.error("conditionalOauth failed: ${exception.message}", exception)
            response.sendRedirect(getFrontendUrl(request) + "/login?error=${exception.message}")
          }
        }

    } else {
      http
    }
  }

  private fun handleSuccess(
    request: HttpServletRequest,
    response: HttpServletResponse,
    authentication: Authentication?
  ) {
    runBlocking {
      coroutineScope {
        val oauthToken = authentication as OAuth2AuthenticationToken
        val user = resolveUser(oauthToken)

        log.info("jwt from user ${user.id}")
        val client: OAuth2AuthorizedClient = authorizedClientService.loadAuthorizedClient(
          oauthToken.getAuthorizedClientRegistrationId(),
          oauthToken.getName()
        )

        val accessToken: String = client.getAccessToken().getTokenValue()
        val githubCapability = createGithubCapability(accessToken)
        val userCapability = createUserCapability(user)
        val groupCapability = createGroupCapability(user)
        val jwt = jwtTokenIssuer.createJwtForCapabilities(listOf(githubCapability, userCapability, groupCapability))
        response.addCookie(cookieProvider.createTokenCookie(jwt))
        response.addCookie(cookieProvider.createExpiredSessionCookie("JSESSION"))
        //
        if (environment.acceptsProfiles(Profiles.of(AppProfiles.DEV_ONLY))) {
          response.sendRedirect("http://localhost:4200/?token=${jwt.tokenValue}")
        } else {
          response.sendRedirect(getFrontendUrl(request))
        }
      }
    }
  }

  private fun createUserCapability(user: User): UserCapability {
    return UserCapability(user.id);
  }

  private fun createGroupCapability(user: User): GroupCapability {
    return GroupCapability(emptyList());
  }

  private fun createGithubCapability(authToken: String): GithubCapability {
    return GithubCapability(authToken)
  }

  private fun getFrontendUrl(request: HttpServletRequest): String {
    log.debug("getFrontendUrl ${request.requestURL}")
    val url = URI(request.requestURI).toURL()
    return url.protocol + "://" + url.host.replace("api.", "")
  }

  private suspend fun resolveUser(authentication: OAuth2AuthenticationToken): User {
    val attributes = (authentication.principal as DefaultOAuth2User).attributes
    val email = attributes["email"] as String?
    val githubId = (attributes["id"] as Int).toString()
    return resolveUserByGithubId(githubId)?.also { userUseCase!!.updateLegacyUser(it.id, githubId) }
      ?: userUseCase!!.createUser(
        email = email,
        githubId = githubId,
      )
  }

  private suspend fun resolveUserByGithubId(githubId: String): User? = withContext(Dispatchers.IO) {
    userRepository.findByGithubId(githubId)
      .also {
        it?.let {
          meterRegistry.counter(AppMetrics.userLogin).increment()
        }
      }
  }

  @Bean
  fun userDetailsService(@Value("\${app.actuatorPassword}") actuatorPassword: String): InMemoryUserDetailsManager {
    val user: BasicAuthUserDetails = BasicAuthUser
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
