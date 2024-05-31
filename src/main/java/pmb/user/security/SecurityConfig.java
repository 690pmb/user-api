package pmb.user.security;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import pmb.user.dto.UserDto;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private static final String ADMIN_ROLE = "ADMIN";

  private final MyUserDetailsService myUserDetailsService;
  private final JwtTokenProvider jwtTokenProvider;
  private final String adminUser;
  private final String adminPassword;

  public SecurityConfig(
      MyUserDetailsService myUserDetailsService,
      JwtTokenProvider jwtTokenProvider,
      @Value("${admin.user}") String adminUser,
      @Value("${admin.password}") String adminPassword) {
    this.myUserDetailsService = myUserDetailsService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.adminUser = adminUser;
    this.adminPassword = adminPassword;
  }

  @Bean
  protected InMemoryUserDetailsManager configureAdminUser() {
    myUserDetailsService.save(
        new UserDto(
            this.adminUser,
            this.adminPassword,
            Collections.emptyList(), SecurityConfig.ADMIN_ROLE));
    return new InMemoryUserDetailsManager(
        User.builder()
            .username(this.adminUser)
            .password(this.adminPassword)
            .roles(SecurityConfig.ADMIN_ROLE)
            .build());
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.authorizeRequests(
        authorizeRequests -> authorizeRequests
            .antMatchers("/api-docs*", "/users/signin*", "/users/signup*")
            .permitAll()
            .antMatchers("/apps/**")
            .hasRole(ADMIN_ROLE)
            .anyRequest()
            .authenticated())
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            handling -> handling.authenticationEntryPoint(
                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .addFilterBefore(
            new JwtTokenFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder = http
        .getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(myUserDetailsService)
        .passwordEncoder(passwordEncoder());
    return authenticationManagerBuilder.build();
  }
}
