package com.example.demo.config;

import com.example.demo.filter.CORSFilter;
import com.example.demo.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity(debug = false)
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CORSFilter CORSFilter;
    private final AuthenticationProvider authenticationProvider;
    private final CustomAccessDeniedException customerAccessDeniedException;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(CORSFilter, ChannelProcessingFilter.class);

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/v1/students/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/v1/students").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/students").hasAuthority("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/students/{id}").hasAuthority("ADMIN")
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated());

        http.cors(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .exceptionHandling(exception -> {
            exception.authenticationEntryPoint(customAuthenticationEntryPoint);
            exception.accessDeniedHandler(customerAccessDeniedException);
        });
        return http.build();
    }
}
