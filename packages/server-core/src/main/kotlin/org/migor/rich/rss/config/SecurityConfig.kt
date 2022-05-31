package org.migor.rich.rss.config

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {

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
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())

  }

}
