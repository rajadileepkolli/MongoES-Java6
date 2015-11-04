package com.digitalbridge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.access.vote.RoleHierarchyVoter;
import org.springframework.security.access.vote.RoleVoter;

import com.digitalbridge.security.MongoDBAuthenticationProvider;

/**
 * <p>
 * MongoESConfig class.
 * </p>
 *
 * @author rajakolli
 * @version 1: 0
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableMongoAuditing(modifyOnCreate = false)
@EnableScheduling
public class MongoESConfig extends SpringBootServletInitializer {

  @Lazy @Autowired private MongoDBAuthenticationProvider authenticationProvider;

  /**
   * <p>
   * main.
   * </p>
   *
   * @param args an array of {@link java.lang.String} objects.
   */
  public static void main(String[] args) {
    SpringApplication.run(MongoESConfig.class, args);
  }

  /** {@inheritDoc} */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(MongoESConfig.class);
  }

  /**
   * Spring Security Hierarchy Roles
   *
   * @return a {@link org.springframework.security.access.vote.RoleVoter} object.
   */
  @Bean
  public RoleVoter roleVoter() {
    return new RoleHierarchyVoter(roleHierarchy());
  }

  /**
   * <p>
   * roleHierarchy.
   * </p>
   *
   * @return a {@link org.springframework.security.access.hierarchicalroles.RoleHierarchy} object.
   */
  @Bean
  public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
    String roleHierarchyStringRepresentation = "ROLE_SUPERUSER > ROLE_ADMIN ROLE_ADMIN > ROLE_USER ROLE_USER > ROLE_GUEST";
    roleHierarchy.setHierarchy(roleHierarchyStringRepresentation);
    authenticationProvider.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
    return roleHierarchy;
  }

}
