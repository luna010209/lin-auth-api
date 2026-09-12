package io.lin.auth.config.security;

import io.lin.auth.config.firebase.FirebaseAuthFilter;
import io.lin.auth.config.firebase.FirebaseTokenVerifier;
import io.lin.auth.feature.login.jwt.bToken.TokenProvider;
import io.lin.auth.feature.login.jwt.cJwtConfig.JwtAccessDeniedHandler;
import io.lin.auth.feature.login.jwt.cJwtConfig.JwtAuthenticationEntryPoint;
import io.lin.auth.feature.login.jwt.cJwtConfig.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            FirebaseAuthFilter firebaseAuthFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new JwtFilter(tokenProvider), FirebaseAuthFilter.class)
                // CORS Filter
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/health",
                                "/region",
                                "/error",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/index.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()

                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public FirebaseAuthFilter firebaseAuthFilter(
            FirebaseTokenVerifier firebaseTokenVerifier
    ) {
        return new FirebaseAuthFilter(firebaseTokenVerifier);
    }
}
