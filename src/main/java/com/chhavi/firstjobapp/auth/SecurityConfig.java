package com.chhavi.firstjobapp.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // ── 1. Public ──
                        .requestMatchers("/", "/index.html", "/*.html", "/*.js", "/*.css", "/*.ico").permitAll()
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // ── 2. REVIEWS (most specific — must come before company rules) ──
                        // GET reviews — both roles can view
                        .requestMatchers(HttpMethod.GET,    "/companies/*/reviews").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/companies/*/reviews/*").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        // POST / PUT / DELETE reviews — USER only, admin cannot write reviews
                        .requestMatchers(HttpMethod.POST,   "/companies/*/reviews").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.PUT,    "/companies/*/reviews/*").hasAuthority("ROLE_USER")
                        .requestMatchers(HttpMethod.DELETE, "/companies/*/reviews/*").hasAuthority("ROLE_USER")

                        // ── APPLICATIONS ──
                        .requestMatchers(HttpMethod.POST,   "/applications/apply/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/applications/my").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/applications/check/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/applications/job/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/applications").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/applications/resume/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/applications/**").hasAuthority("ROLE_ADMIN")

                        // ── APPLICATIONS ──
                        .requestMatchers(HttpMethod.POST,   "/applications/apply/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/applications/my").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        // ── 2. AI Assistant Endpoints ──
                        .requestMatchers("/api/v1/ai/**").authenticated()

                        // ── 3. JOBS ──
                        .requestMatchers(HttpMethod.GET,    "/jobs").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/jobs/*").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/jobs").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/jobs/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/jobs/*").hasAuthority("ROLE_ADMIN")

                        // ── 4. COMPANIES (/* not /** so review URLs never match here) ──
                        .requestMatchers(HttpMethod.GET,    "/companies").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET,    "/companies/*").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/companies").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT,    "/companies/*").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/companies/*").hasAuthority("ROLE_ADMIN")

                        // ── 5. Everything else requires login ──
                        .anyRequest().hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}