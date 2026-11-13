package com.mysite.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 일단 모든 요청 허용 (로그인이 필요한 기능은 3-08에서 @PreAuthorize 로 막는다)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll())
                // H2 콘솔은 스프링 시큐리티의 CSRF 토큰을 모르므로 예외 처리
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**"))
                // H2 콘솔 화면은 frame 으로 구성 → 같은 출처의 frame 은 허용
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));
        return http.build();
    }
}
