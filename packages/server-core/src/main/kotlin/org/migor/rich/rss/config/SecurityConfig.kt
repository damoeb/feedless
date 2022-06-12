package org.migor.rich.rss.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

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

  @Throws(Exception::class)
  override fun configure(http: HttpSecurity) {
    http
      .authorizeRequests()
      .antMatchers("/api/intern/**")
      .authenticated()
      .and()
      .csrf()
//// todo also crf protect these api/feeds/discover, api/feeds/to-permanent
//      .requireCsrfProtectionMatcher { it.servletPath.startsWith("/api/auth")}
      .csrfTokenRepository(getCookieCsrfTokenRepository())
  }

  @Bean
  fun getCookieCsrfTokenRepository(): CookieCsrfTokenRepository {
    return CookieCsrfTokenRepository()
  }

}
