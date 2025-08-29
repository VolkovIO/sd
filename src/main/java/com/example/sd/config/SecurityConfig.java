package com.example.sd.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Публичные эндпоинты
                        .requestMatchers(HttpMethod.GET, "/api/adverts", "/api/adverts/**").permitAll()

                        // Защищенные эндпоинты чатов
                        .requestMatchers(HttpMethod.GET, "/api/chats/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/chats/**").authenticated()

                        // Защищенные эндпоинты объявлений
                        .requestMatchers(HttpMethod.POST, "/api/adverts").authenticated()

                        .anyRequest().permitAll()
                )
                .httpBasic(httpBasic -> {})
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
