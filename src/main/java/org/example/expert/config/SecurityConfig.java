package org.example.expert.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                // JWT 인증은 서버가 로그인 세션을 저장하지 않으므로 세션을 생성하지 않도록 설정합니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // JWT를 먼저 검증해서 SecurityContext에 인증 정보를 넣은 뒤 권한 검사를 진행합니다.
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        // 기존 JwtFilter의 /admin 접근 제한을 Spring Security 권한 검사로 옮긴 부분입니다.
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // /admin 이외의 사용자 API는 기존처럼 로그인한 사용자라면 접근할 수 있게 유지합니다.
                        .requestMatchers("/users/**").authenticated()
                        .anyRequest().authenticated()
                )
                .build();
    }
}
