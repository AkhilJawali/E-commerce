package com.shopease.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application.
 * 
 * <p>Local dev configuration with permitAll for cart endpoints.
 * TODO: Add JWT filter when Auth module is built.</p>
 * 
 * @author akhil jawali
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Merge endpoint requires authentication
                .requestMatchers("/api/v1/cart/merge").authenticated()
                // All other cart endpoints are open (guest support)
                .requestMatchers("/api/v1/cart/**").permitAll()
                .anyRequest().authenticated()
            );

        // TODO: Add JWT filter here when Auth module is built
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
