package org.migor.rich.rss.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

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
      .csrf().disable()
      .authorizeRequests()
//      .antMatchers("/actuator/**").hasRole("ENDPOINT_ADMIN")
      .antMatchers("/api/intern/**").authenticated()
  }
}
