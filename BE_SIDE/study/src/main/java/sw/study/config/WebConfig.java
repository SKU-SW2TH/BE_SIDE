package sw.study.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import sw.study.config.jwt.JWTService;
import sw.study.config.jwt.JwtFilter;
import sw.study.config.jwt.TokenProvider;
import sw.study.user.service.MemberDetailsServiceImpl;
import sw.study.user.util.RedisUtil;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WebConfig {
    private final MemberDetailsServiceImpl memberDetailsService;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final JWTService jwtService;

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/static/**");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // HTTP 기본 인증 비활성화
        http.httpBasic(AbstractHttpConfigurer::disable)
                // CSRF 보호 비활성화 (REST API의 경우 일반적으로 비활성화)
                .csrf(AbstractHttpConfigurer::disable)
                // 요청에 대한 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 특정 경로에 대한 접근 허용
                        .requestMatchers("/api/auth/**", "/api/member/**").permitAll()
                        // 나머지 모든 요청을 허용 (이 부분은 필요에 따라 수정 가능)
                        .anyRequest().permitAll()
                )
                // 세션 관리 설정 (무상태 세션)
                .sessionManagement(configurer -> configurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 상태를 유지하지 않는 세션 정책
                // 기본 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 로그아웃 비활성화
                .logout(AbstractHttpConfigurer::disable);
        // .logout(logout -> logout // 로그아웃 설정
        //         .logoutSuccessUrl("/login") // 로그아웃 성공 후 리다이렉트할 URL
        //         .invalidateHttpSession(true) // 세션 무효화
        // );

        // JwtFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(new JwtFilter(tokenProvider, redisUtil, jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 보안 필터 체인을 빌드하여 반환
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(memberDetailsService).passwordEncoder(bCryptPasswordEncoder);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        //configuration.setAllowedOrigins(List.of("*"));  // 모든 출처 허용
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
