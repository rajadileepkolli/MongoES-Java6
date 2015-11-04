package com.digitalbridge.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.AccessDeniedHandler;

import com.digitalbridge.util.Constants;

/**
 * <p>
 * SecurityConfiguration class.
 * </p>
 *
 * @author rajakolli
 * @version 1 : 1
 */
// @EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true)
@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired private MongoDBAuthenticationProvider authenticationProvider;

  /** {@inheritDoc} */
  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/js/**", "/css/**");
  }

  // http://www.itpro.co.uk/databases/24738/mongodb-will-encrypt-your-data-to-protect-it-from-hackers

  /**
   * {@inheritDoc} This section defines the security policy for the app. - BASIC authentication is supported (enough for
   * this REST-based demo) - /employees is secured using URL security shown below - CSRF headers are disabled since we
   * are only testing the REST interface, not a web one. NOTE: GET is not shown which defaults to permitted.
   */
  /*
   * @Override protected void configure(HttpSecurity http) throws Exception {
   *
   * http.httpBasic().and().authorizeRequests().antMatchers(HttpMethod.POST,
   * "/assetwrapper/search/**") .hasRole("ADMIN").antMatchers(HttpMethod.GET,
   * "/assetwrapper/search/**").hasAnyRole("ADMIN", "USER")
   * .antMatchers(HttpMethod.PUT, "/assetwrapper/search/**").hasRole("ADMIN")
   * .antMatchers(HttpMethod.PATCH,
   * "/assetwrapper/search/**").hasRole("ADMIN").and().csrf().disable(); }
   */

  /**
   * {@inheritDoc} This section defines the user accounts which can be used for authentication as well as the roles each
   * user has.
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {

    AccessDeniedHandler accessDeniedHandler = new MyAccessDeniedHandler();

    http.httpBasic().and().authorizeRequests()
        /*.antMatchers("/index.html", "/api/", "/").permitAll()*/
        .anyRequest().fullyAuthenticated().and().exceptionHandling().accessDeniedHandler(accessDeniedHandler).and()
        .csrf().disable().sessionManagement().maximumSessions(Constants.ONE).maxSessionsPreventsLogin(true).and()
        .sessionFixation().newSession().and().headers().httpStrictTransportSecurity();
  }

  /**
   * <p>
   * configureGlobal.
   * </p>
   *
   * @param auth a
   *          {@link org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder}
   *          object.
   * @throws java.lang.Exception if any.
   */
  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
    auth.authenticationProvider(authenticationProvider);
  }
}
