package com.mysite.sbb;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 컨트롤러 메서드의 @PreAuthorize 동작
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 정적 파일·H2 콘솔은 경로 단위로 항상 허용 (14168 46강)
                // 정적 파일을 static/resource/ 아래에 모아 두어서 "/resource/**" 하나로 허용할 수 있다
                // 나머지는 일단 모두 허용 (로그인이 필요한 기능은 @PreAuthorize 로 막는다)
                // → 나중에 "/**" 를 authenticated() 로 바꿔도 CSS·JS·로그인 화면 꾸밈은 깨지지 않는다
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/favicon.ico", "/resource/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/**").permitAll())
                // H2 콘솔은 스프링 시큐리티의 CSRF 토큰을 모르므로 예외 처리
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**"))
                // H2 콘솔 화면은 frame 으로 구성 → 같은 출처의 frame 은 허용
                .headers(headers -> headers
                        .addHeaderWriter(new XFrameOptionsHeaderWriter(
                                XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)))
                // 로그인 : GET /user/login 은 우리 화면, POST /user/login 은 시큐리티가 처리
                .formLogin(formLogin -> formLogin
                        .loginPage("/user/login")
                        .defaultSuccessUrl("/"))
                // 로그아웃 : POST /user/logout (CSRF 토큰 필요 → 다른 사이트가 몰래 로그아웃시키지 못함)
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true));
        return http.build();
    }

    // 비밀번호 단방향 해시 (같은 비밀번호도 매번 다른 salt 로 다른 값이 저장됨)
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
