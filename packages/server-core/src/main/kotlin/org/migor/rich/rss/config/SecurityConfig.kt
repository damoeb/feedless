package org.migor.rich.rss.config

import jakarta.servlet.http.Cookie
import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.auth.JwtRequestFilter
import org.migor.rich.rss.service.AuthService
import org.migor.rich.rss.service.PropertyService
import org.migor.rich.rss.service.TokenProvider
import org.migor.rich.rss.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.web.SecurityFilterChain


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
      .sessionManagement()
//      .sessionFixation()
        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        .and()
      .csrf()
        .disable()
      .formLogin()
        .disable()
      .httpBasic()
        .disable()
//      .addFilterAfter(jwtRequestFilter, AnonymousAuthenticationFilter::class.java)
      .addFilterAfter(jwtRequestFilter, OAuth2LoginAuthenticationFilter::class.java)
      .oauth2Login()
      .successHandler { request, response, authentication ->
        run {
          val attributes = (authentication.principal as DefaultOAuth2User).attributes
          val email = attributes["email"] as String
//          // other attributes: given_name, locale, first_name
          val name = attributes["name"] as String

          val user = userService.findByEmail(email)
            .orElseGet { userService.createUser(name, email, "") }
          log.info("jwt from user ${user.id}")
          val jwt = tokenProvider.createJwtForUser(user)
          val tokenCookie = toTokenCookie(jwt)
          response.addCookie(tokenCookie)
          response.addCookie(removeSessionCookie())

          if (environment.acceptsProfiles(Profiles.of(AppProfiles.dev))) {
            response.sendRedirect("http://localhost:4200/?token=${jwt.tokenValue}")
          } else {
            request.getRequestDispatcher("/").forward(request, response)
          }
        }
      }
      .failureHandler { _, _, exception -> log.error(exception.message) }
//      .defaultSuccessUrl("/me")
//      .failureUrl("/loginFailure")
//      .tokenEndpoint()
//      .and()
      .and()
//      .userDetailsService()
      .anonymous()
//      .authorizeHttpRequests()
////      .antMatchers("/actuator/**").hasRole("ENDPOINT_ADMIN")
//      .requestMatchers("/api/intern/**").authenticated()
//      .requestMatchers("/api/oauth2/**").anonymous()
      .and()
      .build()
  }

  private fun toTokenCookie(authToken: Jwt): Cookie {
    val cookie = Cookie("TOKEN", authToken.tokenValue)
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = tokenProvider.getTokenExpiration().seconds.toInt()
    return cookie
  }

  private fun removeSessionCookie(): Cookie {
    val cookie = Cookie("JSESSION", "")
    cookie.isHttpOnly = true
    cookie.domain = propertyService.domain
    cookie.maxAge = 0
    return cookie
  }
}
